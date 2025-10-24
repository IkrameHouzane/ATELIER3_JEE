package ma.fstt.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import ma.fstt.model.*;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Bean SessionScoped pour gérer le panier d'achat
 * SessionScoped = Le panier reste actif pendant toute la session utilisateur
 */
@Named
@SessionScoped
@Getter
@Setter
public class PanierBean implements Serializable {

    @PersistenceContext(unitName = "mycnx")
    private EntityManager em;

    @Inject
    private AuthBean authBean; // Pour récupérer l'utilisateur connecté

    private Panier panierActif;

    @PostConstruct
    public void init() {
        // Le panier sera créé lors de la première utilisation
    }

    /**
     * Récupère le panier actif (le crée si nécessaire)
     */
    public Panier getPanierActif() {
        if (panierActif == null && authBean.estConnecte()) {
            chargerOuCreerPanier();
        }
        return panierActif;
    }

    /**
     * Charge le panier actif de l'utilisateur ou en crée un nouveau
     */
    @Transactional
    public void chargerOuCreerPanier() {
        if (!authBean.estConnecte()) {
            return;
        }

        // Cherche un panier actif pour cet utilisateur
        List<Panier> paniers = em.createQuery(
                        "SELECT p FROM Panier p LEFT JOIN FETCH p.lignesPanier WHERE p.internaute.id = :internauteId AND p.actif = true",
                        Panier.class)
                .setParameter("internauteId", authBean.getInternauteConnecte().getId())
                .getResultList();

        if (!paniers.isEmpty()) {
            panierActif = paniers.get(0);
        } else {
            // Créer un nouveau panier
            panierActif = new Panier();
            panierActif.setInternaute(authBean.getInternauteConnecte());
            em.persist(panierActif);
        }
    }

    /**
     * Ajoute un produit au panier
     */
    @Transactional
    public String ajouterAuPanier(Long produitId) {
        // Vérifier que l'utilisateur est connecté
        if (!authBean.estConnecte()) {
            return "login?faces-redirect=true";
        }

        try {
            Produit produit = em.find(Produit.class, produitId);
            if (produit == null || produit.getStock() < 1) {
                return null;
            }

            if (panierActif == null) {
                chargerOuCreerPanier();
            }

            // Vérifier si le produit existe déjà dans le panier
            Optional<LignePanier> ligneExistante = panierActif.getLignesPanier().stream()
                    .filter(ligne -> ligne.getProduit().getId().equals(produitId))
                    .findFirst();

            if (ligneExistante.isPresent()) {
                // Augmenter la quantité
                LignePanier ligne = ligneExistante.get();
                if (produit.estDisponible(ligne.getQuantite() + 1)) {
                    ligne.setQuantite(ligne.getQuantite() + 1);
                }
            } else {
                // Créer une nouvelle ligne
                LignePanier nouvelleLigne = new LignePanier();
                nouvelleLigne.setPanier(panierActif);
                nouvelleLigne.setProduit(produit);
                nouvelleLigne.setQuantite(1);
                panierActif.getLignesPanier().add(nouvelleLigne);
                em.persist(nouvelleLigne);
            }

            return "panier?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retire un produit du panier
     */
    @Transactional
    public void retirerDuPanier(Long lignePanierId) {
        try {
            LignePanier ligne = em.find(LignePanier.class, lignePanierId);
            if (ligne != null) {
                panierActif.getLignesPanier().remove(ligne);
                em.remove(ligne);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Modifie la quantité d'un produit dans le panier
     */
    @Transactional
    public void modifierQuantite(Long lignePanierId, Integer nouvelleQuantite) {
        try {
            LignePanier ligne = em.find(LignePanier.class, lignePanierId);
            if (ligne != null && nouvelleQuantite > 0) {
                if (ligne.getProduit().estDisponible(nouvelleQuantite)) {
                    ligne.setQuantite(nouvelleQuantite);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vide complètement le panier
     */
    @Transactional
    public void viderPanier() {
        if (panierActif != null) {
            for (LignePanier ligne : panierActif.getLignesPanier()) {
                em.remove(ligne);
            }
            panierActif.getLignesPanier().clear();
        }
    }

    /**
     * Transforme le panier en commande
     */
    @Transactional
    public String validerCommande() {
        if (panierActif == null || panierActif.getLignesPanier().isEmpty()) {
            return null;
        }

        try {
            // Créer la commande
            Commande commande = new Commande();
            commande.setInternaute(authBean.getInternauteConnecte());
            commande.setMontantTotal(panierActif.getTotal());
            commande.setAdresseLivraison(authBean.getInternauteConnecte().getAdresse());
            em.persist(commande);

            // Copier les lignes du panier vers la commande
            for (LignePanier lignePanier : panierActif.getLignesPanier()) {
                LigneCommande ligneCommande = new LigneCommande();
                ligneCommande.setCommande(commande);
                ligneCommande.setProduit(lignePanier.getProduit());
                ligneCommande.setQuantite(lignePanier.getQuantite());
                ligneCommande.setPrixUnitaire(lignePanier.getProduit().getPrix());

                // Déduire du stock
                Produit produit = lignePanier.getProduit();
                produit.setStock(produit.getStock() - lignePanier.getQuantite());

                commande.getLignesCommande().add(ligneCommande);
                em.persist(ligneCommande);
            }

            // Désactiver le panier
            panierActif.setActif(false);
            panierActif = null;

            return "confirmation?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calcule le nombre total d'articles dans le panier
     */
    public Integer getNombreArticles() {
        if (panierActif == null || panierActif.getLignesPanier() == null) {
            return 0;
        }
        return panierActif.getNombreArticles();
    }

    /**
     * Calcule le montant total du panier
     */
    public Double getTotal() {
        if (panierActif == null) {
            return 0.0;
        }
        return panierActif.getTotal();
    }
}