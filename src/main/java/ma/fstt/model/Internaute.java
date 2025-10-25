package ma.fstt.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité Internaute
 */
@Entity
@Table(name = "internautes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"paniers", "commandes"})
public class Internaute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    @Column(length = 15)
    private String telephone;

    @Column(length = 300)
    private String adresse;

    @Column(length = 100)
    private String ville;

    @Column(length = 20)
    private String codePostal;

    @Column(nullable = false, length = 20)
    private String role = "USER";

    // Relation OneToMany : Un internaute peut avoir plusieurs paniers (historique)
    @OneToMany(mappedBy = "internaute", cascade = CascadeType.ALL)
    private List<Panier> paniers = new ArrayList<>();

    // Relation OneToMany : Un internaute peut avoir plusieurs commandes
    @OneToMany(mappedBy = "internaute", cascade = CascadeType.ALL)
    private List<Commande> commandes = new ArrayList<>();
}
