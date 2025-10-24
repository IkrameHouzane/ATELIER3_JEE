package ma.fstt.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import ma.fstt.model.Categorie;
import ma.fstt.model.Produit;

import java.util.List;

/**
 * Bean pour gérer l'affichage et la gestion des produits (Vitrine)
 * RequestScoped = Nouveau bean à chaque page
 */
@Named
@RequestScoped
@Getter
@Setter
public class ProduitBean {

    @PersistenceContext(unitName = "mycnx")
    private EntityManager em;

    // Pour créer un nouveau produit
    private Produit nouveauProduit = new Produit();

    // Liste de tous les produits (vitrine)
    private List<Produit> produits;

    // Liste des catégories pour le formulaire
    private List<Categorie> categories;

    // Pour la recherche
    private String motCle;

    // ID de catégorie pour filtrer
    private Long categorieId;

    @PostConstruct
    public void init() {
        chargerProduits();
        chargerCategories();
    }

    /**
     * Charge tous les produits
     */
    public void chargerProduits() {
        produits = em.createQuery("SELECT p FROM Produit p ORDER BY p.id DESC", Produit.class)
                .getResultList();
    }

    /**
     * Charge toutes les catégories
     */
    public void chargerCategories() {
        categories = em.createQuery("SELECT c FROM Categorie c ORDER BY c.nom", Categorie.class)
                .getResultList();
    }

    /**
     * Recherche des produits par mot-clé
     */
    public void rechercher() {
        if (motCle == null || motCle.trim().isEmpty()) {
            chargerProduits();
        } else {
            produits = em.createQuery(
                            "SELECT p FROM Produit p WHERE LOWER(p.nom) LIKE LOWER(:motCle) OR LOWER(p.description) LIKE LOWER(:motCle)",
                            Produit.class)
                    .setParameter("motCle", "%" + motCle + "%")
                    .getResultList();
        }
    }

    /**
     * Filtre les produits par catégorie
     */
    public void filtrerParCategorie() {
        if (categorieId == null) {
            chargerProduits();
        } else {
            produits = em.createQuery(
                            "SELECT p FROM Produit p WHERE p.categorie.id = :categorieId",
                            Produit.class)
                    .setParameter("categorieId", categorieId)
                    .getResultList();
        }
    }

    /**
     * Sauvegarde (ajoute ou modifie) un produit
     */
    @Transactional
    public String sauvegarderProduit() {
        try {
            // Récupérer la catégorie sélectionnée
            if (categorieId != null) {
                Categorie categorie = em.find(Categorie.class, categorieId);
                nouveauProduit.setCategorie(categorie);
            }

            if (nouveauProduit.getId() == null) {
                // Nouveau produit
                em.persist(nouveauProduit);
            } else {
                // Modification d'un produit existant
                em.merge(nouveauProduit);
            }
            
            annulerModification(); // Réinitialise le formulaire
            chargerProduits();
            return "admin-produits?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Charge un produit pour modification
     */
    public void modifierProduit(Produit produit) {
        this.nouveauProduit = em.find(Produit.class, produit.getId());
        if (this.nouveauProduit.getCategorie() != null) {
            this.categorieId = this.nouveauProduit.getCategorie().getId();
        } else {
            this.categorieId = null;
        }
    }

    /**
     * Réinitialise le formulaire après ajout/modification ou annulation
     */
    public void annulerModification() {
        this.nouveauProduit = new Produit();
        this.categorieId = null;
    }

    /**
     * Supprime un produit
     */
    @Transactional
    public void supprimerProduit(Long id) {
        try {
            Produit produit = em.find(Produit.class, id);
            if (produit != null) {
                em.remove(produit);
                chargerProduits();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
