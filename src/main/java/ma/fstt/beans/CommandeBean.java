package ma.fstt.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import lombok.Setter;
import ma.fstt.model.Commande;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean pour afficher l'historique des commandes de l'utilisateur
 */
@Named
@RequestScoped
@Getter
@Setter
public class CommandeBean {

    @PersistenceContext(unitName = "mycnx")
    private EntityManager em;

    @Inject
    private AuthBean authBean;

    private List<Commande> mesCommandes = new ArrayList<>();

    @PostConstruct
    public void init() {
        if (authBean.estConnecte()) {
            chargerMesCommandes();
        }
    }

    /**
     * Charge toutes les commandes de l'utilisateur connecté
     */
    public void chargerMesCommandes() {
        mesCommandes = em.createQuery(
                        "SELECT DISTINCT c FROM Commande c LEFT JOIN FETCH c.lignesCommande WHERE c.internaute.id = :internauteId ORDER BY c.dateCommande DESC",
                        Commande.class)
                .setParameter("internauteId", authBean.getInternauteConnecte().getId())
                .getResultList();
    }
}
