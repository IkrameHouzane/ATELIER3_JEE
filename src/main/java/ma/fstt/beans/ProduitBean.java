package ma.fstt.beans;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import ma.fstt.model.Categorie;
import ma.fstt.model.Produit;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped  // ← CHANGÉ ICI
@Getter
@Setter
public class ProduitBean implements Serializable {  // ← AJOUTÉ Serializable

    private static final long serialVersionUID = 1L;  // ← AJOUTÉ

    @PersistenceContext(unitName = "mycnx")
    private EntityManager em;

    private Produit nouveauProduit = new Produit();
    private List<Produit> produits;
    private List<Categorie> categories;
    private String motCle;
    private Long categorieId;

    @PostConstruct
    public void init() {
        chargerProduits();
        chargerCategories();
    }

    public void chargerProduits() {
        try {
            produits = em.createQuery("SELECT p FROM Produit p ORDER BY p.id DESC", Produit.class)
                    .getResultList();
        } catch (Exception e) {
            addErrorMessage("Erreur chargement : " + e.getMessage());
        }
    }

    public void chargerCategories() {
        try {
            categories = em.createQuery("SELECT c FROM Categorie c ORDER BY c.nom", Categorie.class)
                    .getResultList();
        } catch (Exception e) {
            addErrorMessage("Erreur catégories : " + e.getMessage());
        }
    }

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
            addErrorMessage("Erreur recherche : " + e.getMessage());
        }
    }

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
            addErrorMessage("Erreur filtrage : " + e.getMessage());
        }
    }

    @Transactional
    public void ajouterProduit() {  // ← CHANGÉ en void
        try {
            if (categorieId != null) {
                Categorie categorie = em.find(Categorie.class, categorieId);
                nouveauProduit.setCategorie(categorie);
            }

            if (nouveauProduit.getId() == null) {
                em.persist(nouveauProduit);
                addInfoMessage("Produit ajouté !");
            } else {
                em.merge(nouveauProduit);
                addInfoMessage("Produit modifié !");
            }

            nouveauProduit = new Produit();
            categorieId = null;
            chargerProduits();
        } catch (Exception e) {
            addErrorMessage("Erreur : " + e.getMessage());
        }
    }

    public void modifierProduit(Produit produit) {
        this.nouveauProduit = em.find(Produit.class, produit.getId());
        this.categorieId = nouveauProduit.getCategorie() != null ?
                nouveauProduit.getCategorie().getId() : null;
    }

    public void annulerModification() {
        this.nouveauProduit = new Produit();
        this.categorieId = null;
    }

    @Transactional
    public void supprimerProduit(Long id) {
        try {
            Produit produit = em.find(Produit.class, id);
            if (produit != null) {
                em.createQuery("DELETE FROM LignePanier lp WHERE lp.produit.id = :produitId")
                        .setParameter("produitId", id)
                        .executeUpdate();
                em.createQuery("DELETE FROM LigneCommande lc WHERE lc.produit.id = :produitId")
                        .setParameter("produitId", id)
                        .executeUpdate();
                em.remove(produit);
                chargerProduits();
                addInfoMessage("Produit supprimé !");
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Erreur suppression : " + e.getMessage());
        }
    }

    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", message));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", message));
    }
}