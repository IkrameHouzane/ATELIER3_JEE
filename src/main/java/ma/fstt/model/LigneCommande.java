package ma.fstt.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité LigneCommande
 * Représente une ligne dans une commande
 * IMPORTANT : On sauvegarde le prix au moment de l'achat
 * (car le prix du produit peut changer après)
 */
@Entity
@Table(name = "lignes_commande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"commande", "produit"})
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantite;

    @Column(nullable = false)
    private Double prixUnitaire; // Prix au moment de l'achat (peut être différent du prix actuel du produit)

    // Relation ManyToOne : Une ligne appartient à une commande
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    // Relation ManyToOne : Une ligne concerne un produit
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /**
     * Calcule le sous-total de cette ligne
     * Utilise le prixUnitaire sauvegardé, pas le prix actuel du produit
     */
    public Double getSousTotal() {
        return prixUnitaire * quantite;
    }
}
