package ma.fstt.beans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import ma.fstt.model.Internaute;

import java.io.Serializable;
import java.util.List;

/**
 * Bean pour gérer l'authentification des utilisateurs
 * SessionScoped = Les informations restent pendant toute la session
 */
@Named
@SessionScoped
@Getter
@Setter
public class AuthBean implements Serializable {

    @PersistenceContext(unitName = "mycnx")
    private EntityManager em;

    // Pour le formulaire de connexion
    private String email;
    private String motDePasse;

    // Pour le formulaire d'inscription
    private Internaute nouvelInternaute = new Internaute();

    // L'utilisateur actuellement connecté
    private Internaute internauteConnecte;

    // Message d'erreur à afficher
    private String messageErreur;

    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean estConnecte() {
        return internauteConnecte != null;
    }

    /**
     * Connexion d'un utilisateur
     * Retourne la page de redirection ou null en cas d'erreur
     */
    public String seConnecter() {
        try {
            // Recherche l'utilisateur dans la base de données
            List<Internaute> internautes = em.createQuery(
                            "SELECT i FROM Internaute i WHERE i.email = :email AND i.motDePasse = :motDePasse",
                            Internaute.class)
                    .setParameter("email", email)
                    .setParameter("motDePasse", motDePasse)
                    .getResultList();

            if (internautes.isEmpty()) {
                messageErreur = "Email ou mot de passe incorrect";
                return null;
            }

            // Connexion réussie
            internauteConnecte = internautes.get(0);
            messageErreur = null;

            // Redirection vers la page d'accueil
            return "index?faces-redirect=true";

        } catch (Exception e) {
            messageErreur = "Erreur lors de la connexion";
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Inscription d'un nouvel utilisateur
     */
    @Transactional
    public String sInscrire() {
        try {
            // Vérifier si l'email existe déjà
            List<Internaute> internautes = em.createQuery(
                            "SELECT i FROM Internaute i WHERE i.email = :email",
                            Internaute.class)
                    .setParameter("email", nouvelInternaute.getEmail())
                    .getResultList();

            if (!internautes.isEmpty()) {
                messageErreur = "Cet email est déjà utilisé";
                return null;
            }

            // Créer le nouvel utilisateur
            em.persist(nouvelInternaute);

            // Connexion automatique après inscription
            internauteConnecte = nouvelInternaute;
            nouvelInternaute = new Internaute(); // Réinitialiser le formulaire
            messageErreur = null;

            return "index?faces-redirect=true";

        } catch (Exception e) {
            messageErreur = "Erreur lors de l'inscription";
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Déconnexion
     */
    public String seDeconnecter() {
        internauteConnecte = null;
        email = null;
        motDePasse = null;
        return "index?faces-redirect=true";
    }
}
