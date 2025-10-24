package ma.fstt.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité Produit
 * Représente un produit dans la vitrine du site e-commerce
 */
@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "categorie")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double prix;

    @Column(nullable = false)
    private Integer stock = 0; // Quantité disponible en stock

    @Column(length = 500)
    private String photo; // URL ou chemin de l'image du produit

    // Relation ManyToOne : Un produit appartient à une catégorie
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    /**
     * Méthode utile pour vérifier si le produit est disponible en quantité suffisante
     */
    public boolean estDisponible(int quantite) {
        return this.stock >= quantite;
    }
}
