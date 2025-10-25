package ma.fstt.beans;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import lombok.Setter;
import ma.fstt.model.Categorie;

import java.util.List;

@Named
@Stateless
@Getter
@Setter
public class CategorieBean {

    @PersistenceContext(unitName = "mycnx")
    private EntityManager em;

    private Categorie nouvelleCategorie = new Categorie();
    private List<Categorie> categories;

    @PostConstruct
    public void init() {
        chargerCategories();
    }

    public void chargerCategories() {
        try {
            categories = em.createQuery("SELECT c FROM Categorie c ORDER BY c.nom", Categorie.class)
                    .getResultList();
        } catch (Exception e) {
            addErrorMessage("Erreur lors du chargement : " + e.getMessage());
        }
    }

    public void ajouterCategorie() {
        try {
            if (nouvelleCategorie.getId() == null) {
                em.persist(nouvelleCategorie);
                addInfoMessage("Catégorie ajoutée avec succès");
            } else {
                em.merge(nouvelleCategorie);
                addInfoMessage("Catégorie modifiée avec succès");
            }
            nouvelleCategorie = new Categorie();
            chargerCategories();
        } catch (Exception e) {
            addErrorMessage("Erreur : " + e.getMessage());
        }
    }

    public void modifierCategorie(Categorie categorie) {
        this.nouvelleCategorie = em.find(Categorie.class, categorie.getId());
    }

    public void annulerModification() {
        this.nouvelleCategorie = new Categorie();
    }

    public void supprimerCategorie(Long id) {
        try {
            Categorie categorie = em.find(Categorie.class, id);
            if (categorie != null) {
                em.remove(categorie);
                addInfoMessage("Catégorie supprimée");
                chargerCategories();
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Impossible de supprimer : " + e.getMessage());
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
