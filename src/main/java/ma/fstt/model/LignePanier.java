package ma.fstt.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité LignePanier
 * Représente une ligne dans un panier (un produit + sa quantité)
 * Exemple : 3x iPhone 15 Pro
 */
@Entity
@Table(name = "lignes_panier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"panier", "produit"})
public class LignePanier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantite;

    // Relation ManyToOne : Une ligne appartient à un panier
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panier_id", nullable = false)
    private Panier panier;

    // Relation ManyToOne : Une ligne concerne un produit
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /**
     * Calcule le sous-total de cette ligne
     * Exemple : 3 produits à 100 DH = 300 DH
     */
    public Double getSousTotal() {
        return produit.getPrix() * quantite;
    }
}