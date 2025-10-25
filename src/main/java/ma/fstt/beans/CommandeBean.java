package ma.fstt.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import ma.fstt.model.Commande;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bean pour afficher l'historique des commandes de l'utilisateur et gérer les commandes (admin)
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

    // Pour l'utilisateur normal
    private List<Commande> mesCommandes = new ArrayList<>();

    // Pour l'administrateur
    private List<Commande> toutesLesCommandes = new ArrayList<>();

    @PostConstruct
    public void init() {
        if (authBean.estConnecte()) {
            // Si l'utilisateur est un admin, charger toutes les commandes
            if ("ADMIN".equals(authBean.getInternauteConnecte().getRole())) {
                chargerToutesLesCommandes();
            } else {
                // Sinon, ne charger que ses propres commandes
                chargerMesCommandes();
            }
        }
    }

    /**
     * Charge toutes les commandes de l'utilisateur connecté
     */
    public void chargerMesCommandes() {
        mesCommandes = em.createQuery(
                        "SELECT DISTINCT c FROM Commande c LEFT JOIN FETCH c.lignesCommande LEFT JOIN FETCH c.lignesCommande.produit WHERE c.internaute.id = :internauteId ORDER BY c.dateCommande DESC",
                        Commande.class)
                .setParameter("internauteId", authBean.getInternauteConnecte().getId())
                .getResultList();
    }

    /**
     * Charge absolument toutes les commandes pour l'admin
     */
    public void chargerToutesLesCommandes() {
        toutesLesCommandes = em.createQuery(
                        "SELECT DISTINCT c FROM Commande c LEFT JOIN FETCH c.internaute LEFT JOIN FETCH c.lignesCommande LEFT JOIN FETCH c.lignesCommande.produit ORDER BY c.dateCommande DESC",
                        Commande.class)
                .getResultList();
    }

    /**
     * Met à jour le statut d'une commande
     */
    @Transactional
    public void mettreAJourStatut(Commande commande) {
        em.merge(commande);
    }

    /**
     * Retourne tous les statuts de commande possibles
     */
    public List<Commande.StatutCommande> getStatuts() {
        return Arrays.asList(Commande.StatutCommande.values());
    }
}
