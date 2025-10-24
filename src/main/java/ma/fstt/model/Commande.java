package ma.fstt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Commande
 * Représente une commande passée par un utilisateur
 * Créée quand l'utilisateur valide son panier
 */
@Entity
@Table(name = "commandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"internaute", "lignesCommande"})
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateCommande;

    @Column(nullable = false)
    private Double montantTotal;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Column(length = 300)
    private String adresseLivraison;

    // Relation ManyToOne : Une commande appartient à un internaute
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internaute_id", nullable = false)
    private Internaute internaute;

    // Relation OneToMany : Une commande contient plusieurs lignes
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignesCommande = new ArrayList<>();

    /**
     * Méthode appelée automatiquement avant la persistance
     * Initialise la date de commande
     */
    @PrePersist
    protected void onCreate() {
        dateCommande = LocalDateTime.now();
    }

    /**
     * Énumération pour le statut de la commande
     */
    public enum StatutCommande {
        EN_ATTENTE,      // Commande créée, en attente de confirmation
        CONFIRMEE,       // Commande confirmée
        EN_PREPARATION,  // En cours de préparation
        EXPEDIEE,        // Commande expédiée
        LIVREE,          // Commande livrée
        ANNULEE          // Commande annulée
    }
}