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

import java.util.List;

/**
 * Bean pour gérer les catégories de produits
 * RequestScoped = Créé à chaque requête HTTP
 */
@Named
@RequestScoped
@Getter
@Setter
public class CategorieBean {

    @PersistenceContext(unitName = "mycnx")
    private EntityManager em;

    // Pour créer une nouvelle catégorie
    private Categorie nouvelleCategorie = new Categorie();

    // Liste de toutes les catégories
    private List<Categorie> categories;

    /**
     * Méthode appelée automatiquement après la création du bean
     */
    @PostConstruct
    public void init() {
        chargerCategories();
    }

    /**
     * Charge toutes les catégories depuis la base de données
     */
    public void chargerCategories() {
        categories = em.createQuery("SELECT c FROM Categorie c ORDER BY c.nom", Categorie.class)
                .getResultList();
    }

    /**
     * Sauvegarde (ajoute ou modifie) une catégorie
     */
    @Transactional
    public String sauvegarderCategorie() {
        try {
            if (nouvelleCategorie.getId() == null) {
                // Nouvelle catégorie
                em.persist(nouvelleCategorie);
            } else {
                // Modification d'une catégorie existante
                em.merge(nouvelleCategorie);
            }
            
            annulerModification(); // Réinitialise le formulaire
            chargerCategories();
            return "categories?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Charge une catégorie pour modification
     */
    public void modifierCategorie(Categorie categorie) {
        this.nouvelleCategorie = em.find(Categorie.class, categorie.getId());
    }

    /**
     * Réinitialise le formulaire après ajout/modification ou annulation
     */
    public void annulerModification() {
        this.nouvelleCategorie = new Categorie();
    }

    /**
     * Supprime une catégorie
     */
    @Transactional
    public void supprimerCategorie(Long id) {
        try {
            Categorie categorie = em.find(Categorie.class, id);
            if (categorie != null) {
                em.remove(categorie);
                chargerCategories();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}