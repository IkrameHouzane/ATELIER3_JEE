package ma.fstt.beans;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
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

    // Pour créer ou modifier un produit
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
        try {
            produits = em.createQuery("SELECT p FROM Produit p ORDER BY p.id DESC", Produit.class)
                    .getResultList();
        } catch (Exception e) {
            addErrorMessage("Erreur lors du chargement des produits : " + e.getMessage());
        }
    }

    /**
     * Charge toutes les catégories
     */
    public void chargerCategories() {
        try {
            categories = em.createQuery("SELECT c FROM Categorie c ORDER BY c.nom", Categorie.class)
                    .getResultList();
        } catch (Exception e) {
            addErrorMessage("Erreur lors du chargement des catégories : " + e.getMessage());
        }
    }

    /**
     * Recherche des produits par mot-clé
     */
    public void rechercher() {
        try {
            if (motCle == null || motCle.trim().isEmpty()) {
                chargerProduits();
            } else {
                produits = em.createQuery(
                                "SELECT p FROM Produit p WHERE LOWER(p.nom) LIKE LOWER(:motCle) OR LOWER(p.description) LIKE LOWER(:motCle)",
                                Produit.class)
                        .setParameter("motCle", "%" + motCle + "%")
                        .getResultList();
            }
        } catch (Exception e) {
            addErrorMessage("Erreur lors de la recherche des produits : " + e.getMessage());
        }
    }

    /**
     * Filtre les produits par catégorie
     */
    public void filtrerParCategorie() {
        try {
            if (categorieId == null) {
                chargerProduits();
            } else {
                produits = em.createQuery(
                                "SELECT p FROM Produit p WHERE p.categorie.id = :categorieId",
                                Produit.class)
                        .setParameter("categorieId", categorieId)
                        .getResultList();
            }
        } catch (Exception e) {
            addErrorMessage("Erreur lors du filtrage par catégorie : " + e.getMessage());
        }
    }

    /**
     * Ajoute ou modifie un produit
     */
    @Transactional
    public String ajouterProduit() {
        try {
            if (categorieId != null) {
                Categorie categorie = em.find(Categorie.class, categorieId);
                nouveauProduit.setCategorie(categorie);
            }
            if (nouveauProduit.getId() == null) {
                em.persist(nouveauProduit); // Nouveau produit
            } else {
                em.merge(nouveauProduit); // Mise à jour
            }
            nouveauProduit = new Produit();
            categorieId = null;
            addInfoMessage("Produit enregistré avec succès");
            return "admin-produits?faces-redirect=true";
        } catch (Exception e) {
            addErrorMessage("Erreur lors de l'enregistrement du produit : " + e.getMessage());
            return null;
        }
    }

    /**
     * Charge un produit pour modification
     */
    public void modifierProduit(Produit produit) {
        this.nouveauProduit = em.find(Produit.class, produit.getId());
        this.categorieId = nouveauProduit.getCategorie() != null ? nouveauProduit.getCategorie().getId() : null;
    }

    /**
     * Réinitialise le formulaire
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
                // Supprimer les lignes de commande associées, si nécessaire
                em.createQuery("DELETE FROM LigneCommande lc WHERE lc.produit.id = :produitId")
                        .setParameter("produitId", id)
                        .executeUpdate();
                em.remove(produit);
                chargerProduits();
                addInfoMessage("Produit supprimé avec succès");
            } else {
                addErrorMessage("Produit avec ID " + id + " introuvable");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace
            addErrorMessage("Impossible de supprimer le produit : " + e.getMessage());
        }
    }

    /**
     * Ajoute un message d'information
     */
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", message));
    }

    /**
     * Ajoute un message d'erreur
     */
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", message));
    }
}