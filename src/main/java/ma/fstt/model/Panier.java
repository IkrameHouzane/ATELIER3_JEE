package ma.fstt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Panier
 * Représente le panier d'achat d'un utilisateur
 */
@Entity
@Table(name = "paniers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"internaute", "lignesPanier"})
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private Boolean actif = true; // true = panier actif, false = transformé en commande

    // Relation ManyToOne : Un panier appartient à un internaute
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internaute_id", nullable = false)
    private Internaute internaute;

    // Relation OneToMany : Un panier contient plusieurs lignes (produits)
    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LignePanier> lignesPanier = new ArrayList<>();

    /**
     * Méthode appelée automatiquement avant la persistance
     * Initialise la date de création
     */
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    /**
     * Calcule le montant total du panier
     */
    public Double getTotal() {
        return lignesPanier.stream()
                .mapToDouble(LignePanier::getSousTotal)
                .sum();
    }

    /**
     * Calcule le nombre total d'articles dans le panier
     */
    public Integer getNombreArticles() {
        return lignesPanier.stream()
                .mapToInt(LignePanier::getQuantite)
                .sum();
    }
}