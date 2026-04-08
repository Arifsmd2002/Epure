package com.project2.config;

import com.project2.entity.Category;
import com.project2.entity.Order;
import com.project2.entity.Product;
import com.project2.entity.Moodboard;
import com.project2.entity.Role;
import com.project2.entity.User;
import com.project2.repository.*;
import com.project2.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Component
@SuppressWarnings("null")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MoodboardRepository moodboardRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductService productService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("DEBUG: DataInitializer version 1.1 running...");
        if (roleRepository.count() == 0) {
            Role adminRole = roleRepository.save(new Role("ROLE_ADMIN"));
            Role userRole = roleRepository.save(new Role("ROLE_USER"));

            User admin = new User("admin", "admin@epure.com", passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);

            User user = new User("user", "user@epure.com", passwordEncoder.encode("user123"));
            user.setRoles(Set.of(userRole));
            userRepository.save(user);
        }

        // Always re-initialize Storage products to ensure consistent data and avoid duplicates
        Category storageCatAlways = categoryRepository.findByName("Storage").orElseGet(() -> categoryRepository.save(new Category("Storage")));
        initializeStorage(storageCatAlways);

        // Always update Nordic Reading Nook moodboard image and products
        moodboardRepository.findFirstBySlug("nordic-reading-nook").ifPresent(m -> {
            m.setImageUrl("/images/moodboards/reading_nook_new.jpg");
            
            // Re-link core products to ensure they appear in "Products in this Scene"
            Set<Product> products = new HashSet<>();
            productRepository.findByName("Beige Lounge Chair").stream().findFirst().ifPresent(products::add);
            productRepository.findByName("Nordic Table Lamp").stream().findFirst().ifPresent(products::add);
            productRepository.findByName("Oak Coffee Table").stream().findFirst().ifPresent(products::add);
            productRepository.findByName("Minimal Ceramic Vase").stream().findFirst().ifPresent(products::add);
            productRepository.findByName("Nordic Abstract Print").stream().findFirst().ifPresent(products::add);
            productRepository.findByName("Nordic Reading Ensemble").stream().findFirst().ifPresent(products::add);
            
            m.setProducts(products);
            moodboardRepository.save(m);
            System.out.println("DEBUG: Updated Nordic Reading Nook image and product links.");
        });

        // Always re-initialize Decor and Pillows products to ensure new local images and updated pricing are applied
        Category pillowsCat = categoryRepository.findByName("Pillows").orElseGet(() -> categoryRepository.save(new Category("Pillows")));
        initializePillows(pillowsCat);
        
        Category decorCat = categoryRepository.findByName("Decor").orElseGet(() -> categoryRepository.save(new Category("Decor")));
        initializeDecor(decorCat);

        Category lightingCatAlways = categoryRepository.findByName("Lighting").orElseGet(() -> categoryRepository.save(new Category("Lighting")));
        initializeLamps(lightingCatAlways);

        Category wallArtCat = categoryRepository.findByName("Wall Art").orElseGet(() -> categoryRepository.save(new Category("Wall Art")));
        initializeWallArt(wallArtCat);

        Category textilesCat = categoryRepository.findByName("Textiles").orElseGet(() -> categoryRepository.save(new Category("Textiles")));
        initializeTextiles(textilesCat);

        Category sofasCat = categoryRepository.findByName("Sofas").orElseGet(() -> categoryRepository.save(new Category("Sofas")));
        initializeSofas(sofasCat);

        Category bedsCat = categoryRepository.findByName("Beds").orElseGet(() -> categoryRepository.save(new Category("Beds")));
        initializeBeds(bedsCat);

        // Always re-link moodboard products to ensure the "Products in this Scene" section is populated
        initializeMoodboardProductLinks();

        // Explicitly remove the Teal Velvet Pillow if it exists in the database
        productRepository.deleteByNameModifying("Teal Velvet Pillow");

        // Check if we already have the fully expanded set of products (including the gallery)
        long productCount = productRepository.count();
        if (productCount < 75) { 
            Category lighting = categoryRepository.findByName("Lighting").orElseGet(() -> categoryRepository.save(new Category("Lighting")));
            Category decor = categoryRepository.findByName("Decor").orElseGet(() -> categoryRepository.save(new Category("Decor")));
            Category seating = categoryRepository.findByName("Seating").orElseGet(() -> categoryRepository.save(new Category("Seating")));
            Category tables = categoryRepository.findByName("Tables").orElseGet(() -> categoryRepository.save(new Category("Tables")));
            Category wallArt = categoryRepository.findByName("Wall Art").orElseGet(() -> categoryRepository.save(new Category("Wall Art")));
            Category plants = categoryRepository.findByName("Plants").orElseGet(() -> categoryRepository.save(new Category("Plants")));
            Category pillows = categoryRepository.findByName("Pillows").orElseGet(() -> categoryRepository.save(new Category("Pillows")));
            
            Product lamp = productRepository.save(new Product("Nordic Table Lamp",
                "Crafted from solid light oak and hand-blown matte glass, this lamp emits a warm, diffused glow perfect for serene evenings.",
                new BigDecimal("249.00"), "/images/products/lamp.png", lighting, "Sustainable Oak & Matte Glass", "Norway", 9, "warm nordic bright relax"));
            
            Product vase = productRepository.save(new Product("Minimal Ceramic Vase",
                "A hand-sculpted matte plaster piece with soft, organic curves that captures the essence of Scandi minimalism.",
                new BigDecimal("1250.00"), "/images/products/vase.png", decor, "Artisan Plaster", "Sweden", 10, "minimal neutral relax organic vase"));

            Product chair = productRepository.save(new Product("Beige Lounge Chair",
                "Upholstered in premium linen with a solid ash wood frame, designed for maximum comfort and aesthetic harmony.",
                new BigDecimal("1299.00"), "/images/products/chair.png", seating, "Sustainable Ash & Linen", "Denmark", 8, "warm comfort relax minimal"));

            Product table = productRepository.save(new Product("Oak Coffee Table",
                "A low-profile circular table made from FSC-certified white oak, emphasizing natural wood grain and smooth joinery.",
                new BigDecimal("750.00"), "/images/products/table.png", tables, "FSC White Oak", "Norway", 9, "nordic minimal warm wood"));
            Product sofa = productRepository.findByName("Linen Modular Sofa").stream().findFirst().orElse(null);

            Product plant = productRepository.save(new Product("Indoor Olive Tree",
                "A live, young olive tree in a simple stone pot. Brings life and natural harmony to indoor spaces.",
                new BigDecimal("180.00"), "/images/products/plant.png", plants, "Living Plant & Stone Pot", "Spain", 10, "natural green organic"));

            // User Gallery Products
            Product ashChair = productRepository.save(new Product("Ash Wood Sculptural Chair",
                "A masterpiece of Nordic joinery, this chair features smooth curves and a grain-matched finish.",
                new BigDecimal("890.00"), "/images/gallery/chair.png", seating, "Solid Ash Wood", "Sweden", 9, "minimal sculptural warm"));

            Product dresser = productRepository.findByName("Minimalist Oak Dresser").stream().findFirst().orElse(null);

            // Pillows are now initialized via a dedicated method below to ensure consistent pricing and re-initialization.

            // Textiles are now initialized via initializeTextiles() to ensure consistent pricing.

            Product workspace = productRepository.save(new Product("Minimalist Home Workspace",
                "A complete workspace solution including a sleek oak desk and ergonomic task chair for focused efficiency.",
                new BigDecimal("1850.00"), "/images/gallery/workspace.png", tables, "Sustainable Oak & Steel", "Sweden", 9, "minimal functional focus"));

            Product livingScene = productRepository.findByName("Scandinavian Living Ensemble").stream().findFirst().orElse(null);

            Product aestheticScene = productRepository.save(new Product("Aesthetic Design Collection",
                "A statement collection of objects that define the modern Scandinavian minimalist lifestyle.",
                new BigDecimal("2400.00"), "/images/gallery/ChatGPT Image Mar 12, 2026, 01_02_34 PM.png", decor, "Luxury Eco-Materials", "Norway", 10, "aesthetic sculptural elite"));

            // Ensure "sofa" category is renamed to "Sofas" for consistency with UI
            categoryRepository.findByName("sofa").ifPresent(oldCat -> {
                oldCat.setName("Sofas");
                categoryRepository.save(oldCat);
            });

            // Ensure "Lighting" category is named correctly and clean up old "Lamps" name
            categoryRepository.findByName("Lamps").ifPresent(oldCat -> {
                oldCat.setName("Lighting");
                categoryRepository.save(oldCat);
            });

            // Olive Living Room Moodboard Products (Updated with Unsplash URLs)
            Product oliveSofa = productRepository.findByName("Olive Modular Sofa").stream().findFirst().orElse(null);

            Product redPillow = productRepository.save(new Product("Red Accent Pillow",
                "A bold accent of color in rich velvet to ground your minimalist seating.",
                new BigDecimal("55.00"), "https://images.unsplash.com/photo-1579656592043-a20e25a4aa4b", pillows, "Velvet", "Italy", 8, "red accent color luxury pillow"));

            Product oliveBeigeCushion = productRepository.save(new Product("Beige Cushion Pillow",
                "Tactile linen blend cushion in a versatile sand tone.",
                new BigDecimal("45.00"), "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2", pillows, "Linen Blend", "Denmark", 9, "neutral pillow sand cushion"));

            // Blanket moved to initializeTextiles()

            Product blackTable = productRepository.save(new Product("Black Round Coffee Table",
                "A statement centerpiece in matte black ash wood with a low profile.",
                new BigDecimal("450.00"), "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85", tables, "Sustainable Ash Wood", "Denmark", 9, "black table minimal dark"));

            Product redChair = productRepository.save(new Product("Red Lounge Chair",
                "An iconic silhouette offering a striking contrast to the olive and wood tones.",
                new BigDecimal("750.00"), "https://images.unsplash.com/photo-1616628182506-6a3d6f6b8b32", seating, "Upholstered Steel Frame", "Denmark", 9, "red chair seating modern"));

            Product cabinet = productRepository.findByName("Wooden Storage Cabinet").stream().findFirst().orElse(null);

            // Wooden Storage Cabinet now handled by initializeStorage()

            Product wallDecor = productRepository.save(new Product("Woven Wall Decor",
                "Organic circular wall art that brings natural texture to the vertical plane.",
                new BigDecimal("95.00"), "https://images.unsplash.com/photo-1519710164239-da123dc03ef4", decor, "Seagrass", "India", 10, "wall art woven natural"));

            Product pendant = productRepository.save(new Product("White Pendant Light",
                "Atmospheric sphere light that casts a soft, dreamlike glow.",
                new BigDecimal("110.00"), "https://images.unsplash.com/photo-1513506003901-1e6a229e2d15", lighting, "Rice Paper", "Japan", 9, "white lamp pendant soft"));

            Product olivePlant = productRepository.save(new Product("Indoor Olive Tree Plant",
                "A mature indoor olive tree providing vertical greenery and a Mediterranean touch.",
                new BigDecimal("120.00"), "https://images.unsplash.com/photo-1501004318641-b39e6451bec6", plants, "Living Plant", "Spain", 10, "green plant nature tree"));

            // Update Moodboards with Gallery Scenes - use slug for identification to avoid duplicates
            Moodboard sthlm = moodboardRepository.findFirstBySlug("stockholm-morning-set")
                .orElseGet(() -> moodboardRepository.findByNameContainingIgnoreCase("Stockholm Morning").stream().findFirst()
                .orElse(new Moodboard("Stockholm Morning Set",
                    "Inspired by the soft, diffused light of a Swedish dawn. This collection features neutral tones and raw wood textures.",
                    "/images/moodboards/stockholm_morning_scene.jpg")));
            
            sthlm.setSlug("stockholm-morning-set");
            sthlm.getProducts().clear();
            Product art = productRepository.findByName("Nordic Abstract Print").stream().findFirst()
                .orElseGet(() -> productRepository.save(new Product("Nordic Abstract Print",
                    "A curated abstract print in a sustainable oak frame, designed to bring a sense of calm to your walls.",
                    new BigDecimal("150.00"), "/images/products/wall-art/wall-art-1.avif", wallArt, "Recycled Paper & Oak", "Denmark", 9, "minimal aesthetic art")));

            sthlm.setProducts(new HashSet<>(List.of(lamp, vase, table, sofa, ashChair, dresser, aestheticScene, plant, art)));
            moodboardRepository.save(sthlm);

            Moodboard livingMood = moodboardRepository.findFirstBySlug("nordic-living-sanctuary")
                .orElseGet(() -> moodboardRepository.findByNameContainingIgnoreCase("Living Sanctuary").stream().findFirst()
                .orElse(new Moodboard("Nordic Living Sanctuary",
                "Create a peaceful center in your home with curated textures and warm wood accents.",
                "/images/gallery/living room.png")));
            
            livingMood.getProducts().clear();
            Product forestGreenPillow = productRepository.findByName("Forest Green Velvet Pillow").stream().findFirst().orElse(null);
            Product naturalWovenPillow = productRepository.findByName("Natural Woven Pillow").stream().findFirst().orElse(null);
            Product shawlSetLookedUp = productRepository.findByName("Nordic Shawl Collection").stream().findFirst().orElse(null);
            Product textileBundleLookedUp = productRepository.findByName("Artisan Textile Bundle").stream().findFirst().orElse(null);
            
            Set<Product> livingProducts = new HashSet<>();
            if (sofa != null) livingProducts.add(sofa);
            if (chair != null) livingProducts.add(chair);
            if (table != null) livingProducts.add(table);
            if (forestGreenPillow != null) livingProducts.add(forestGreenPillow);
            if (shawlSetLookedUp != null) livingProducts.add(shawlSetLookedUp);
            if (livingScene != null) livingProducts.add(livingScene);
            if (naturalWovenPillow != null) livingProducts.add(naturalWovenPillow);
            if (textileBundleLookedUp != null) livingProducts.add(textileBundleLookedUp);
            
            livingMood.setProducts(livingProducts);
            livingMood.setSlug("nordic-living-sanctuary");
            moodboardRepository.save(livingMood);

            Moodboard nook = moodboardRepository.findFirstBySlug("nordic-reading-nook")
                .orElseGet(() -> moodboardRepository.findByNameContainingIgnoreCase("Reading Nook").stream().findFirst()
                .orElse(new Moodboard("Nordic Reading Nook",
                "A curated sanctuary for relaxation and intellectual focus, centered around the warmth of natural materials.",
                "/images/moodboards/reading_nook.png")));
            
            nook.getProducts().clear();
            Product naturalWovenPillowNook = productRepository.findByName("Natural Woven Pillow").stream().findFirst().orElse(null);
            Product shawlSetNook = productRepository.findByName("Nordic Shawl Collection").stream().findFirst().orElse(null);
            
            Set<Product> nookProducts = new HashSet<>();
            if (chair != null) nookProducts.add(chair);
            if (lamp != null) nookProducts.add(lamp);
            if (table != null) nookProducts.add(table);
            if (ashChair != null) nookProducts.add(ashChair);
            if (shawlSetNook != null) nookProducts.add(shawlSetNook);
            if (workspace != null) nookProducts.add(workspace);
            if (naturalWovenPillowNook != null) nookProducts.add(naturalWovenPillowNook);
            
            nook.setProducts(nookProducts);
            nook.setSlug("nordic-reading-nook");
            moodboardRepository.save(nook);

            // New Olive Moodboard
            Moodboard oliveMood = moodboardRepository.findFirstBySlug("olive-living-room")
                .orElseGet(() -> moodboardRepository.findByNameContainingIgnoreCase("Olive Living Room").stream().findFirst()
                .orElse(new Moodboard("Olive Living Room Moodboard",
                "A modern olive-green living space combining warm textures, natural materials, and Scandinavian minimalism.",
                "/images/moodboards/olive_living_room.png")));
            
            oliveMood.getProducts().clear();
            Product mustardBlanketLookedUp = productRepository.findByName("Mustard Throw Blanket").stream().findFirst().orElse(null);
            
            Set<Product> oliveProducts = new HashSet<>();
            if (oliveSofa != null) oliveProducts.add(oliveSofa);
            if (redPillow != null) oliveProducts.add(redPillow);
            if (oliveBeigeCushion != null) oliveProducts.add(oliveBeigeCushion);
            if (mustardBlanketLookedUp != null) oliveProducts.add(mustardBlanketLookedUp);
            if (blackTable != null) oliveProducts.add(blackTable);
            if (redChair != null) oliveProducts.add(redChair);
            if (cabinet != null) oliveProducts.add(cabinet);
            if (wallDecor != null) oliveProducts.add(wallDecor);
            if (pendant != null) oliveProducts.add(pendant);
            if (olivePlant != null) oliveProducts.add(olivePlant);
            
            oliveMood.setProducts(oliveProducts);
            oliveMood.setSlug("olive-living-room");
            moodboardRepository.save(oliveMood);

            // New Emerald Dream Bedroom (Standardized 16:9)
            Moodboard emeraldMood = moodboardRepository.findFirstBySlug("emerald-dream-bedroom")
                .orElseGet(() -> new Moodboard("Emerald Dream Bedroom",
                "A sophisticated sanctuary featuring deep emerald velvet textures and warm ambient lighting. Designed for ultimate rest and luxury.",
                "/images/moodboards/moodboard_1.png"));
            
            emeraldMood.setSlug("emerald-dream-bedroom");
            Set<Product> emeraldProducts = new HashSet<>();
            productRepository.findByName("Forest Green Velvet Pillow").stream().findFirst().ifPresent(emeraldProducts::add);
            productRepository.findByName("White Pendant Light").stream().findFirst().ifPresent(emeraldProducts::add);
            productRepository.findByName("Emerald Velvet Dream Bed").stream().findFirst().ifPresent(emeraldProducts::add);
            
            emeraldMood.setProducts(emeraldProducts);
            moodboardRepository.save(emeraldMood);

            // Cleanup any duplicate moodboards that might have been created
            cleanupDuplicateMoodboards();

            // Ensure Lighting products are initialized
            // Re-initialization of Lighting, Tables, etc. moved outside or kept here based on need.
            // Keeping other categories for initial setup only, but Lighting is now always re-initialized above.

            // Ensure Table products are initialized
            Category tablesCat = categoryRepository.findByName("Tables").orElseGet(() -> categoryRepository.save(new Category("Tables")));
            initializeTables(tablesCat);

            // Ensure Plant products are initialized
            Category plantsCat = categoryRepository.findByName("Plants").orElseGet(() -> categoryRepository.save(new Category("Plants")));
            initializePlants(plantsCat);

            // Ensure Seating products are initialized
            Category seatingCat = categoryRepository.findByName("Seating").orElseGet(() -> categoryRepository.save(new Category("Seating")));
            initializeSeating(seatingCat);
        }
        
        // Ensure Storage products are always correctly up-to-date (redundant call ensure cleanup run after setup if setup skipped)
        Category storageAlwaysFinal = categoryRepository.findByName("Storage").orElseGet(() -> categoryRepository.save(new Category("Storage")));
        initializeStorage(storageAlwaysFinal);

        // Always deduplicate and fix moodboard slugs on every startup
        cleanupDuplicateMoodboards();
        updateMoodboardSlugs();

        // Always update Stockholm Morning Set image to ensure user changes are reflected
        moodboardRepository.findFirstBySlug("stockholm-morning-set").ifPresent(m -> {
            m.setImageUrl("/images/moodboards/stockholm_morning_scene.jpg");
            moodboardRepository.save(m);
        });

        // Initialize a mock order for testing chatbot flows if none exist
        if (orderRepository.count() == 0) {
            Order testOrder = new Order();
            testOrder.setFirstName("Erik");
            testOrder.setLastName("Sorenson");
            testOrder.setEmail("erik@example.com");
            testOrder.setTotalAmount(new BigDecimal("1250.00"));
            testOrder.setOrderStatus("ORDER_PLACED");
            testOrder.setPaymentStatus("SUCCESS");
            orderRepository.save(testOrder);
        }
    }

    private void initializeLamps(Category lighting) {
        // 1. Safe Cleanup of existing "Lighting" products
        productService.bulkDeleteByCategory("Lighting");
        productService.bulkDeleteByCategory("Lamps");

        // 2. Insert 20 Unique Lamp Products
        // Floor Lamps (4)
        productRepository.save(new Product("Scandinavian Tripod Floor Lamp", "Elegant tripod base with a soft linen shade.", new BigDecimal("8500.00"), "/images/products/lamps-collection/lamp-1.jpg", lighting, "Oak & Linen", "Norway", 9, "bright nordic minimal", 4.8));
        productRepository.save(new Product("Modern Brass Standing Lamp", "Sleek brass finish for a modern luxury vibe.", new BigDecimal("9200.00"), "/images/products/lamps-collection/modern-brass-pendant.png", lighting, "Brushed Brass", "Sweden", 8, "bright modern minimal", 4.7));
        productRepository.save(new Product("Arc Designer Floor Lamp", "Iconic overhanging design for focused lounge lighting.", new BigDecimal("11500.00"), "/images/products/lamps-collection/lamp-3.jpg", lighting, "Steel & Marble", "Italy", 9, "bright modern arc", 4.9));
        productRepository.save(new Product("Slim Tripod Nordic Lamp", "Slim tripod silhouette with a contemporary shade.", new BigDecimal("7800.00"), "/images/products/lamps-collection/black-slim-pendant.png", lighting, "Powder Coated Metal", "Denmark", 9, "bright slim nordic", 4.6));

        // Table Lamps (4)
        productRepository.save(new Product("Artisan Wooden Table Lamp", "Compact wooden base with a warm diffused glow.", new BigDecimal("3200.00"), "/images/products/lamps-collection/lamp-5.jpg", lighting, "Solid Ash", "Sweden", 10, "bright minimal wood", 4.9));
        productRepository.save(new Product("Elegant Brass Desk Lamp", "Adjustable task lamp for the modern home office.", new BigDecimal("4500.00"), "/images/products/lamps-collection/lamp-6.jpg", lighting, "Antique Brass", "Denmark", 8, "bright task focus", 4.6));
        productRepository.save(new Product("Modern Globe Table Lamp", "Frosted glass orb on a minimal metal base.", new BigDecimal("4100.00"), "/images/products/lamps-collection/modern-globe-pendant.jpg", lighting, "Frosted Glass", "Norway", 9, "bright globe warm", 4.7));
        productRepository.save(new Product("Nordic Bedside Lamp", "Soft lighting for a peaceful bedroom atmosphere.", new BigDecimal("2800.00"), "/images/products/lamps-collection/lamp-8.jpg", lighting, "Ceramic & Linen", "Sweden", 9, "bright soft relax", 4.8));

        // Pendant Lamps (4)
        productRepository.save(new Product("Industrial Dome Pendant", "Clean architectural lines in a matte finish.", new BigDecimal("5200.00"), "/images/products/lamps-collection/lamp-9.jpg", lighting, "Matte Aluminum", "Denmark", 9, "bright minimal dome", 4.6));
        productRepository.save(new Product("Natural Rattan Pendant", "Natural textures for a warm, organic feel.", new BigDecimal("6800.00"), "/images/products/lamps-collection/lamp-10.jpg", lighting, "Natural Rattan", "Indonesia", 9, "bright natural organic", 4.7));
        productRepository.save(new Product("Airy Glass Pendant", "Airy and light, inspired by traditional lanterns.", new BigDecimal("4500.00"), "/images/products/lamps-collection/lamp-11.jpg", lighting, "Handmade Paper", "Japan", 10, "bright airy soft", 4.9));
        productRepository.save(new Product("Opal Sphere Pendant", "Timeless opal glass sphere for even illumination.", new BigDecimal("7400.00"), "/images/products/lamps-collection/lamp-12.jpg", lighting, "Opal Glass", "Finland", 9, "bright sphere modern", 4.8));

        // Decorative Lamps (4)
        productRepository.save(new Product("Geometric Sconce Lamp", "Intricate shadows for an atmospheric evening.", new BigDecimal("3500.00"), "/images/products/lamps-collection/lamp-13.jpg", lighting, "Wrought Iron", "Norway", 9, "bright lattice decor", 4.8));
        productRepository.save(new Product("Botanical Accent Lamp", "A sculptural piece that glows with soft light.", new BigDecimal("4200.00"), "/images/products/lamps-collection/lamp-14.jpg", lighting, "Hand-blown Glass", "Sweden", 9, "bright aesthetic sculptural", 4.7));
        productRepository.save(new Product("Sculptural Stone Lamp", "Hand-crafted ceramic base with organic curves.", new BigDecimal("4800.00"), "/images/products/lamps-collection/lamp-15.jpg", lighting, "Artisan Ceramic", "Denmark", 10, "bright organic ceramic", 4.9));
        productRepository.save(new Product("Cybernetic Line Lamp", "A single line of light for ultra-modern spaces.", new BigDecimal("5500.00"), "/images/products/lamps-collection/yellow-table-lamp.jpg", lighting, "Anodized Aluminum", "Finland", 9, "bright minimal led", 4.6));

        // Outdoor Lamps (4)
        productRepository.save(new Product("Glow Pillar Garden Lamp", "Weatherproof stone lamp for a natural patio glow.", new BigDecimal("5800.00"), "/images/products/lamps-collection/floor-lamp-shadow.jpg", lighting, "Natural Stone", "Norway", 10, "bright outdoor organic", 4.9));
        productRepository.save(new Product("Nordic Path LED Lamp", "High-efficiency lighting for garden paths.", new BigDecimal("8200.00"), "/images/products/lamps-collection/hand-sconce-lamp.jpg", lighting, "Weatherproof Steel", "Sweden", 9, "bright garden nordic", 4.7));
        productRepository.save(new Product("Lakeside Patio Lantern", "Classic lantern silhouette with a modern LED twist.", new BigDecimal("6900.00"), "/images/products/lamps-collection/lamp-19.jpg", lighting, "Teak & Glass", "Denmark", 8, "bright patio modern", 4.8));
        productRepository.save(new Product("Eco Solar Garden Pillar", "Eco-friendly lighting for sustainable landscapes.", new BigDecimal("7500.00"), "/images/products/lamps-collection/lamp-20.jpg", lighting, "Recycled Polymer", "Finland", 8, "bright pillar minimal", 4.6));
    }

    private void initializeTables(Category tables) {
        // 1. Safe Cleanup of existing "Tables" products
        productService.bulkDeleteByCategory("Tables");

        // 2. Insert 8 New Unique Table Products
        productRepository.save(new Product("Scandinavian White Dining Table", "A clean white dining table with tapered oak legs, perfect for family meals.", new BigDecimal("12500.00"), "/images/products/tables-collection/table-1.jpg", tables, "Laminate & Oak", "Sweden", 10, "bright minimal dining", 4.9));
        productRepository.save(new Product("Natural Wood Utility Table", "A solid communal table with a raw wood finish and sturdy joinery.", new BigDecimal("18200.00"), "/images/products/tables-collection/table-2.jpg", tables, "Solid Pine", "Norway", 9, "nordic organic communal", 4.8));
        productRepository.save(new Product("Modern Round Coffee Table", "A low-profile round table with a subtle edge detail and tripod base.", new BigDecimal("5400.00"), "/images/products/tables-collection/table-3.jpg", tables, "FSC Oak", "Denmark", 8, "minimal warm round", 4.7));
        productRepository.save(new Product("Artistic Side Table", "A sculptural side piece in a soft burnt orange matte finish.", new BigDecimal("3200.00"), "/images/products/tables-collection/table-4.jpg", tables, "Coated Metal", "Sweden", 9, "modern accent pop", 4.6));
        productRepository.save(new Product("Nordic Minimal Side Table", "A versatile side table in light ash with integrated storage tray.", new BigDecimal("4100.00"), "/images/products/tables-collection/table-5.jpg", tables, "Solid Ash", "Norway", 10, "minimal soft functional", 4.9));
        productRepository.save(new Product("Scandinavian Nightstand", "A clean-lined nightstand with a slim profile and natural grain.", new BigDecimal("2800.00"), "/images/products/tables-collection/table-6.jpg", tables, "Oak Veneer", "Finland", 9, "minimal bedside crisp", 4.8));
        productRepository.save(new Product("Sleek Home Workspace Desk", "An airy white desk designed for focus and productivity.", new BigDecimal("9500.00"), "/images/products/tables-collection/table-7.jpg", tables, "Steel & Laminate", "Denmark", 9, "bright task work", 4.7));
        productRepository.save(new Product("Artisan Work Table", "A handcrafted wooden table with a large surface area for creators.", new BigDecimal("14800.00"), "/images/products/tables-collection/table-8.jpg", tables, "Solid Beech", "Finland", 10, "nordic artisan studio", 4.9));
    }

    private void initializePlants(Category plants) {
        // 1. Safe Cleanup of existing "Plants" products
        productService.bulkDeleteByCategory("Plants");

        // 2. Insert 8 New Unique Plant Products
        productRepository.save(new Product("Architectural Snake Plant", "A striking, low-maintenance plant with vertical sword-like leaves.", new BigDecimal("150.00"), "/images/products/plants-collection/plant-1.jpg", plants, "Living Plant", "Norway", 9, "bright minimal vertical", 4.8));
        productRepository.save(new Product("Lush Monstera Deliciosa", "The iconic Swiss cheese plant, bringing a bold jungle vibe to your space.", new BigDecimal("280.00"), "/images/products/plants-collection/plant-2.jpg", plants, "Living Plant", "Sweden", 10, "bright organic bold", 4.9));
        productRepository.save(new Product("Stately Fiddle Leaf Fig", "A tall, elegant fig tree with large, violin-shaped glossy leaves.", new BigDecimal("350.00"), "/images/products/plants-collection/plant-3.jpg", plants, "Living Plant", "Denmark", 9, "bright stately tree", 4.7));
        productRepository.save(new Product("Charming Pilea Peperomioides", "The 'Chinese Money Plant' with unique circular, pancake-shaped leaves.", new BigDecimal("85.00"), "/images/products/plants-collection/plant-4.jpg", plants, "Living Plant", "Finland", 10, "bright playful organic", 4.8));
        productRepository.save(new Product("Minimalist Desert Cactus", "A sculptural desert beauty that thrives in bright, sunny spots.", new BigDecimal("120.00"), "/images/products/plants-collection/plant-5.jpg", plants, "Living Plant", "Mexico", 9, "bright sculptural desert", 4.6));
        productRepository.save(new Product("Modern Succulent Mix", "A curated arrangement of hardy succulents for a clean tabletop accent.", new BigDecimal("65.00"), "/images/products/plants-collection/plant-6.jpg", plants, "Living Plant", "Norway", 8, "bright table accent", 4.7));
        productRepository.save(new Product("Bold Zebra Plant", "Striking white-veined leaves that add a graphic, natural element.", new BigDecimal("95.00"), "/images/products/plants-collection/plant-7.jpg", plants, "Living Plant", "Sweden", 9, "bright graphic nature", 4.8));
        productRepository.save(new Product("Elegant Peace Lily", "A graceful plant with dark green leaves and brilliant white flowers.", new BigDecimal("110.00"), "/images/products/plants-collection/plant-8.jpg", plants, "Living Plant", "Denmark", 10, "bright elegant floral", 4.9));
    }

    private void initializeSeating(Category seating) {
        // 1. Safe Cleanup of existing "Seating" products
        productService.bulkDeleteByCategory("Seating");

        // 2. Insert 8 New Unique Seating Products (Lounge Chairs in different colors)
        productRepository.save(new Product("Mustard Nordic Lounge Chair", "A bright mustard yellow lounge chair with a comfortable ottoman, adding a pop of sunshine.", new BigDecimal("1850.00"), "/images/products/seating-collection/chair-1.jpg", seating, "Premium Fabric & Birch", "Sweden", 10, "bright scandinavian cozy mustard", 4.9));
        productRepository.save(new Product("Charcoal Minimalist Armchair", "A sophisticated charcoal grey armchair featuring clean lines and a weighted minimalist design.", new BigDecimal("1450.00"), "/images/products/seating-collection/chair-2.jpg", seating, "Weighted Fabric & Steel", "Denmark", 9, "minimal modern charcoal scandi", 4.7));
        productRepository.save(new Product("Navy Blue Winged Chair", "An elegant navy blue winged armchair that offers a deep, grounded sense of relaxation.", new BigDecimal("1650.00"), "/images/products/seating-collection/chair-3.jpg", seating, "Velvet & Walnut", "Norway", 8, "luxury elegant navy comfort", 4.8));
        productRepository.save(new Product("Dusty Rose Modern Chair", "A soft dusty rose armchair with a modern silhouette, perfect for a cozy corner.", new BigDecimal("1350.00"), "/images/products/seating-collection/chair-4.jpg", seating, "Wool Blend & Ash", "Sweden", 9, "soft modern rose minimal", 4.6));
        productRepository.save(new Product("Olive Green Leather Chair", "A deep olive green designer leather chair with a sleek architectural frame.", new BigDecimal("2400.00"), "/images/products/seating-collection/chair-5.jpg", seating, "Top-Grain Leather & Matte Steel", "Finland", 10, "nordic green leather premium", 4.9));
        productRepository.save(new Product("Sage Green Designer Chair", "A set of Scandinavian sage green design chairs that blend organic tones with functional comfort.", new BigDecimal("950.00"), "/images/products/seating-collection/chair-6.jpg", seating, "Molded Poly & Beech", "Sweden", 9, "sage minimalist design organic", 4.7));
        productRepository.save(new Product("Terracotta Scandi Lounge Chair", "A vibrant terracotta lounge chair that brings warmth and energy to any neutral room.", new BigDecimal("1550.00"), "/images/products/seating-collection/chair-7.jpg", seating, "Structured Cotton & Pine", "Norway", 9, "warm scandinavian terracotta bright", 4.8));
        productRepository.save(new Product("Warm Brown Artisan Chair", "A classic warm brown leather lounge chair with artisan-crafted wooden accents.", new BigDecimal("2800.00"), "/images/products/seating-collection/chair-8.jpg", seating, "Hand-finished Leather & Teak", "Denmark", 10, "classic vintage luxury brown", 5.0));
    }

    private void initializeDecor(Category decor) {
        // ... (existing logic)
        productService.bulkDeleteByCategory("Decor");

        // 2. Insert 10 Unique Vase Products (₹1000–₹2500)
        // Using newly imported local images from ProjectEPURE\src\main\resources\static\images\products\vases
        
        productRepository.save(new Product("Terracotta Earth Vase",
            "A tall, handcrafted terracotta-toned vase that adds organic warmth to any shelf or mantle.",
            new BigDecimal("1200.00"), "/images/products/vases/photo-1526198330131-9b0bc79625e4.avif",
            decor, "Artisan Clay", "Norway", 9, "warm terracotta earthy decor vase", 4.8));

        productRepository.save(new Product("Rustic Clay Pot Vase",
            "A raw, natural clay vase with an unglazed matte finish evoking the beauty of aged ceramics.",
            new BigDecimal("1450.00"), "/images/products/vases/photo-1581783342308-f792dbdd27c5.avif",
            decor, "Raw Clay", "Denmark", 9, "rustic clay natural minimal vase", 4.7));

        productRepository.save(new Product("Slate Grey Ceramic Vase",
            "A sleek, matte slate grey ceramic vase for a sophisticated minimalist interior.",
            new BigDecimal("1650.00"), "/images/products/vases/photo-1597825510535-8d713104b5e1.avif",
            decor, "Matte Ceramic", "Denmark", 10, "minimal grey ceramic modern vase", 4.9));

        productRepository.save(new Product("Blush Pink Porcelain Vase",
            "Delicate blush pink fine porcelain — a soft, romantic accent for Nordic-inspired spaces.",
            new BigDecimal("1850.00"), "/images/products/vases/photo-1612196808214-b8e1d6145a8c.avif",
            decor, "Fine Porcelain", "Sweden", 8, "soft pink porcelain romantic vase", 4.6));

        productRepository.save(new Product("Charcoal Midnight Vase",
            "A deep charcoal-black textured vase that makes a bold, sculptural statement on any surface.",
            new BigDecimal("2100.00"), "/images/products/vases/photo-1631125915902-d8abe9225ff2.avif",
            decor, "Textured Ceramic", "Finland", 10, "bold black charcoal minimal vase luxury", 4.9));

        productRepository.save(new Product("Sand Beige Stoneware Vase",
            "A beautiful coastal sand beige stoneware vase with subtle grogged texture.",
            new BigDecimal("1350.00"), "/images/products/vases/photo-1674390521253-79caf82d0ceb.avif",
            decor, "Grogged Stoneware", "Sweden", 9, "natural beige coastal stoneware vase", 4.8));

        productRepository.save(new Product("Olive Green Square Vase",
            "A modern square-profile olive green stoneware vase that captures organic studio light.",
            new BigDecimal("1550.00"), "/images/products/vases/photo-1687191883721-257d8cad5b54.avif",
            decor, "Glazed Stoneware", "Norway", 9, "green olive square stoneware vase", 4.7));

        productRepository.save(new Product("Midnight Blue Hand-blown Vase",
            "A sophisticated midnight blue hand-blown glass vase with a luxurious satin finish.",
            new BigDecimal("2250.00"), "/images/products/vases/premium_photo-1676836059659-161e0d6ea49e.avif",
            decor, "Hand-blown Glass", "Finland", 9, "blue glass blown luxury elegant vase", 4.7));

        productRepository.save(new Product("Clear Crystal Bud Vase",
            "Minimal clear crystal glass bud vase — perfect for a single stem of your favourite flower.",
            new BigDecimal("1050.00"), "/images/products/vases/premium_photo-1682539426214-29b32deeecb4.avif",
            decor, "Crystal Glass", "Sweden", 8, "clear crystal glass minimal bud vase", 4.5));

        productRepository.save(new Product("Forest Green Artisan Vase",
            "A forest-green recycled glass vase with organic bubbles and imperfections that celebrate craftsmanship.",
            new BigDecimal("2450.00"), "/images/products/vases/premium_photo-1682944652547-1d1e950e15c6.avif",
            decor, "Recycled Glass", "Denmark", 10, "green forest recycled artisan premium vase", 5.0));
    }

    private void initializePillows(Category pillows) {
        // 1. Safe Cleanup of existing "Pillows" products
        productService.bulkDeleteByCategory("Pillows");

        // 2. Insert Pillow Products with Price Range ₹500–₹1000
        productRepository.save(new Product("Forest Green Velvet Pillow",
            "Plush Italian velvet in a deep forest green, adding a touch of sophisticated color to neutral settings.",
            new BigDecimal("750.00"), "/images/products/forest_green_pillow.png", pillows, "Premium Velvet", "Italy", 7, "warm color luxury pillow velvet decor cushion", 4.8));

        productRepository.save(new Product("Natural Woven Pillow",
            "Textured cotton weave that brings a tactile, artisan feel to your curated lounge space.",
            new BigDecimal("650.00"), "/images/products/woven_pillow.png", pillows, "Artisan Cotton", "India", 10, "neutral texture natural pillow cushion decor", 4.7));

        productRepository.save(new Product("Soft Beige Cushion",
            "An elegant beige cushion with a soft, tactile texture, designed to complement light wood and neutral tones.",
            new BigDecimal("550.00"), "/images/products/beige_cushion.png", pillows, "Textured Linen Blend", "Denmark", 10, "neutral texture natural pillow cushion decor beige home", 4.9));

        productRepository.save(new Product("Sage Green Velvet Pillow",
            "Bring a touch of nature indoors with this premium sage green velvet pillow, offering both comfort and style.",
            new BigDecimal("820.00"), "/images/products/sage_green_pillow.png", pillows, "Soft Velvet", "Sweden", 9, "natural green organic pillow velvet cushion decor sage", 4.8));

        productRepository.save(new Product("RED accent pillow",
            "A small, energetic red accent pillow to provide a pop of color in minimalist settings.",
            new BigDecimal("500.00"), "/images/products/red_pillow.png",
            pillows, "Velvet", "Finland", 8, "red accent pop", 4.6));

        productRepository.save(new Product("Beige Cushion Pillow",
            "A versatile sand-toned cushion that adds comfort and warmth to your curated sofa arrangement.",
            new BigDecimal("600.00"), "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?auto=format&fit=crop&q=80&w=800",
            pillows, "Linen Blend", "Denmark", 9, "neutral sand cushion", 4.7));

        productRepository.save(new Product("Teal Velvet Pillow",
            "A luxurious teal velvet pillow with a deep, rich luster.",
            new BigDecimal("950.00"), "https://images.unsplash.com/photo-1540555700478-4be289fbecee?auto=format&fit=crop&q=80&w=800", 
            pillows, "Premium Velvet", "Italy", 8, "teal luxury velvet", 4.9));
            
        productRepository.save(new Product("Slate Grey Cushion",
            "A moody grey cushion with a subtle herringbone texture.",
            new BigDecimal("680.00"), "/images/products/grey_cushion.png", 
            pillows, "Wool Blend", "Sweden", 9, "grey minimal mood", 4.8));
    }

    private void initializeWallArt(Category wallArt) {
        // 1. Safe Cleanup of existing "Wall Art" products
        productService.bulkDeleteByCategory("Wall Art");

        // 2. Insert New Wall Art Products with Price Range ₹3000–₹6000
        productRepository.save(new Product("Traditional Pattern Print", "A complex geometric pattern inspired by traditional Nordic textiles.", new BigDecimal("3200.00"), "/images/products/wall-art/wall-art-1.avif", wallArt, "Premium Canvas", "Norway", 10, "nordic pattern traditional", 4.9));
        productRepository.save(new Product("Nordic Wildlife Sketch", "A minimal charcoal sketch of indigenous Nordic wildlife.", new BigDecimal("3800.00"), "/images/products/wall-art/wall-art-2.avif", wallArt, "Handmade Paper", "Sweden", 9, "nature wildlife minimal", 4.8));
        productRepository.save(new Product("Stone Texture Abstract", "A macro-photographic study of ancient granite textures.", new BigDecimal("4150.00"), "/images/products/wall-art/wall-art-3.avif", wallArt, "Fine Art Print", "Finland", 8, "abstract texture stone", 4.7));
        productRepository.save(new Product("Misty Landscape Painting", "A soft watercolor representation of a foggy morning in the fjords.", new BigDecimal("4800.00"), "/images/products/wall-art/wall-art-4.avif", wallArt, "Artisan Paper", "Norway", 10, "landscape mist calm", 4.9));
        productRepository.save(new Product("River Valley Vista", "An expansive view of a winding river through a lush green valley.", new BigDecimal("5400.00"), "/images/products/wall-art/wall-art-5.avif", wallArt, "Cotton Canvas", "Denmark", 9, "landscape nature green", 4.8));
        productRepository.save(new Product("Forest Solitude Portrait", "A moody, atmospheric capture of light filtering through dense pines.", new BigDecimal("5900.00"), "/images/products/wall-art/wall-art-6.avif", wallArt, "Giclée Print", "Sweden", 9, "forest atmospheric moody", 4.6));
        productRepository.save(new Product("Nordic Birds Illustration", "Detailed scientific illustration of seasonal migratory birds.", new BigDecimal("3400.00"), "/images/products/wall-art/wall-art-7.avif", wallArt, "Recycled Paper", "Norway", 10, "birds nature vintage", 4.9));
        productRepository.save(new Product("Countryside Cottage Sketch", "A nostalgic ink drawing of a traditional rural cottage.", new BigDecimal("4200.00"), "/images/products/wall-art/wall-art-8.avif", wallArt, "Textured Paper", "Denmark", 9, "vintage rustic home", 4.7));
        productRepository.save(new Product("Castile Tower Study", "Architectural study of a historic tower in the late afternoon sun.", new BigDecimal("5100.00"), "/images/products/wall-art/wall-art-9.avif", wallArt, "Fine Art Paper", "Spain", 8, "architecture historic study", 4.8));
    }

    private void initializeTextiles(Category textiles) {
        // 1. Safe Cleanup of existing "Textiles" products
        productService.bulkDeleteByCategory("Textiles");

        // 2. Insert Textile Products with Price Range ₹5000–₹6000
        productRepository.save(new Product("Nordic Shawl Collection",
            "A set of three premium wool shawls in complementary earth tones, perfect for styling over seating.",
            new BigDecimal("5200.00"), "/images/products/textiles/nordic_shawl.jpg", textiles, "Recycled Wool", "Norway", 10, "warm cozy earth-tones", 4.8));

        productRepository.save(new Product("Artisan Textile Bundle",
            "A curated mix of linen and cotton textiles, providing varied textures for a harmonious interior atmosphere.",
            new BigDecimal("5450.00"), "/images/products/textiles/artisan_bundle.jpg", textiles, "Linen & Cotton Mix", "Belgium", 9, "minimal texture collection", 4.7));

        productRepository.save(new Product("Mustard Throw Blanket",
            "Hand-woven lambswool throw in a deep ochre, adding warmth and texture.",
            new BigDecimal("5800.00"), "/images/products/textiles/mustard_throw.jpg", textiles, "Lambswool", "Norway", 10, "yellow warm wool blanket", 4.9));

        productRepository.save(new Product("Organic Cotton Throw",
            "A soft, breathable throw made from 100% organic cotton, perfect for year-round comfort.",
            new BigDecimal("5100.00"), "/images/products/textiles/mint_throw.jpg", textiles, "100% Organic Cotton", "Sweden", 9, "minimal natural soft", 4.8));

        productRepository.save(new Product("Linen Table Runner",
            "Elegant linen table runner with hemstitch detail, bringing a touch of refinement to your dining space.",
            new BigDecimal("5350.00"), "/images/products/textiles/table_runner.jpg", textiles, "Stone-washed Linen", "Belgium", 8, "minimal dining elegant", 4.6));

        productRepository.save(new Product("Woolen Blanket Set",
            "A duo of heavy-weight woolen blankets designed for the coldest winter nights.",
            new BigDecimal("5950.00"), "/images/products/textiles/blanket_set.jpg", textiles, "Virgin Wool", "Norway", 10, "warm heavy cozy", 5.0));
    }

    private void cleanupDuplicateMoodboards() {
        List<Moodboard> allMoodboards = moodboardRepository.findAll();
        Set<String> seenSlugs = new HashSet<>();
        
        for (Moodboard m : allMoodboards) {
            String slug = m.getSlug();
            if (slug == null || slug.isEmpty()) {
                // To be handled by updateMoodboardSlugs
                continue;
            }
            
            if (seenSlugs.contains(slug)) {
                // This is a duplicate!
                moodboardRepository.delete(m);
            } else {
                seenSlugs.add(slug);
            }
        }
    }

    private void updateMoodboardSlugs() {
        moodboardRepository.findAll().forEach(m -> {
            if (m.getSlug() == null || m.getSlug().isEmpty()) {
                String slug = m.getName().toLowerCase()
                    .replace(" moodboard", "")
                    .replace(" set", "") 
                    .trim()
                    .replace(" ", "-");
                
                // Specific overrides
                if (m.getName().contains("Stockholm Morning")) slug = "stockholm-morning-set";
                if (m.getName().contains("Living Sanctuary")) slug = "nordic-living-sanctuary";
                
                // Check if this slug is already taken
                final String finalSlug = slug;
                if (moodboardRepository.findFirstBySlug(finalSlug).isEmpty()) {
                    m.setSlug(finalSlug);
                    moodboardRepository.save(m);
                } else {
                    // Append ID to make it unique if name collision
                    m.setSlug(finalSlug + "-" + m.getId());
                    moodboardRepository.save(m);
                }
            }
        });
    }

    private void initializeSofas(Category sofas) {
        // 1. Safe Cleanup of existing "Sofas" products
        productService.bulkDeleteByCategory("Sofas");
        productService.bulkDeleteByCategory("sofa");

        // 2. Insert Core Sofas
        productRepository.save(new Product("Linen Modular Sofa",
            "A deep-seated, ultra-comfortable sofa upholstered in breathable Belgian linen. Its modular design fits any minimalist living space.",
            new BigDecimal("35000.00"), "/images/linen_sofa.png", sofas, "Belgian Linen & Pine Frame", "Belgium", 9, "minimal comfort neutral"));

        productRepository.save(new Product("Scandinavian Living Ensemble",
            "A curated set of living room essentials focusing on light tones and organic materials.",
            new BigDecimal("42000.00"), "/images/gallery/living room.png", sofas, "Mixed Sustainable Materials", "Denmark", 9, "warm complete minimal"));

        productRepository.save(new Product("Nordic Reading Ensemble",
            "A complete reading nook setup featuring a premium lounge chair and matching accessories for the ultimate relaxation scene.",
            new BigDecimal("12000.00"), "/images/gallery/nordic_reading_ensemble.png", sofas, "Premium Materials", "Sweden", 10, "nordic reading ensemble relax complete"));

        productRepository.save(new Product("Olive Modular Sofa",
            "A vast, modular sectional in premium olive upholstery. Designed for both grandeur and intimate comfort.",
            new BigDecimal("32000.00"), "https://images.unsplash.com/photo-1555041469-a586c61ea9bc", sofas, "Premium Fabric & Ash", "Sweden", 10, "green olive comfort large mod"));

        // 3. Insert 10 New Collection Sofas (₹20,000–₹50,000)
        productRepository.save(new Product("Sage Haven 3-Seater", "A serene sage green sofa with deep cushions and a minimalist profile.", new BigDecimal("28500.00"), "/images/products/sofas/sage_green.png", sofas, "Organic Cotton & Ash", "Sweden", 9, "minimal green calm", 4.8));
        productRepository.save(new Product("Olive Grove Modular Sofa", "Versatile modular pieces in a rich olive green, perfect for adaptive living.", new BigDecimal("32000.00"), "/images/products/sofas/olive_green.png", sofas, "Recycled Polyester & Pine", "Norway", 8, "relax organic green", 4.7));
        productRepository.save(new Product("Sand Dune Nordic Sofa", "A warm, sand-colored sofa that brings a coastal Scandinavian vibe to your room.", new BigDecimal("24500.00"), "/images/products/sofas/sand.png", sofas, "Linen Blend & Oak", "Denmark", 10, "nordic warm neutral", 4.9));
        productRepository.save(new Product("Charcoal Loft Sectional", "A sophisticated charcoal grey sofa with a sleek, low-profile design.", new BigDecimal("45000.00"), "/images/products/sofas/charcoal_grey.png", sofas, "Wool Blend & Steel", "Finland", 9, "modern minimal dark", 4.6));
        productRepository.save(new Product("Beige Horizon Sofa", "An elegant beige sofa with clean lines and superior comfort.", new BigDecimal("26800.00"), "/images/products/sofas/beige.png", sofas, "Premium Linen & Ash", "Sweden", 9, "minimal warm neutral", 4.8));
        productRepository.save(new Product("Cream Whisper Sofa", "A soft cream sofa that adds a touch of lightness and airy comfort.", new BigDecimal("23500.00"), "/images/products/sofas/cream.png", sofas, "Tactile Cotton & Beech", "Denmark", 9, "bright airy minimal", 4.5));
        productRepository.save(new Product("Terracotta Earth Sofa", "A bold terracotta sofa inspired by natural clay tones.", new BigDecimal("27900.00"), "/images/products/sofas/terracotta.png", sofas, "Eco-Velvet & Walnut", "Italy", 8, "warm earth organic", 4.7));
        productRepository.save(new Product("Navy Deep Sea Sofa", "A deep navy blue sofa that serves as a grounding centerpiece.", new BigDecimal("34200.00"), "/images/products/sofas/navy_blue.png", sofas, "Structured Twill & Oak", "Norway", 9, "nordic bold calm", 4.9));
        productRepository.save(new Product("Dusty Rose Petal Sofa", "A delicate dusty pink sofa for a soft, contemporary aesthetic.", new BigDecimal("25400.00"), "/images/products/sofas/dusty_pink.png", sofas, "Chenille & Ash", "Sweden", 10, "modern soft warm", 4.8));
        productRepository.save(new Product("Warm Walnut Leather Sofa", "A premium brown leather sofa that gets better with age.", new BigDecimal("48000.00"), "/images/products/sofas/warm_brown.png", sofas, "Top-Grain Leather & Walnut", "Denmark", 7, "luxury vintage warm", 5.0));
    }

    private void initializeStorage(Category storage) {
        // 1. Safe Cleanup of existing "Storage" products to avoid duplicates
        productService.bulkDeleteByCategory("Storage");

        // 2. Core Storage Products
        productRepository.save(new Product("Minimalist Oak Dresser",
            "Three spacious drawers with push-to-open functionality and a clean, handle-less silhouette.",
            new BigDecimal("1450.00"), "/images/gallery/minimalist_oak_table_set.jpg", storage, "FSC White Oak", "Denmark", 8, "minimal storage functional", 4.8));

        productRepository.save(new Product("Wooden Storage Cabinet",
            "A handle-less, clean-lined cabinet in blonde oak for sophisticated organization.",
            new BigDecimal("1150.00"), "https://images.unsplash.com/photo-1598300042247-d088f8ab3a91", storage, "Solid Oak", "Sweden", 9, "storage wood oak cabinet", 4.7));
            
        productRepository.save(new Product("Nordic Slim Credenza",
            "Sleek storage solution for minimalist dining or living areas.",
            new BigDecimal("1850.00"), "https://images.unsplash.com/photo-1595428774223-ef5262412032", storage, "Ash Wood", "Norway", 9, "minimal storage sleek", 4.9));
    }

    private void initializeBeds(Category beds) {
        // 1. Safe Cleanup of existing "Beds" products to avoid duplicates
        productService.bulkDeleteByCategory("Beds");

        // 2. New Premium Bed Products (March 30, 2026)
        productRepository.save(new Product("Celestial Moon Round Bed",
            "An avant-garde circular bed with an integrated moon-phase backlight and ultra-plush cushioning.",
            new BigDecimal("68000.00"), "/images/products/beds/celestial_moon_bed.jpg", beds, "Italian Fabric & Carbon Fiber", "Italy", 9, "modern round celestial bed bright unique", 4.9));

        productRepository.save(new Product("Lavender Dream Modern Bed",
            "A futuristic purple-themed bed with sleek vertical LED lighting and premium grey upholstery.",
            new BigDecimal("54000.00"), "/images/products/beds/lavender_dream_bed.png", beds, "Velvet & Steel", "Sweden", 10, "purple dream modern futuristic bed cozy", 4.8));

        productRepository.save(new Product("Ivory Curved Sculptural Bed",
            "A contemporary curved ivory bed with warm under-bed ambient lighting and a seamless headboard design.",
            new BigDecimal("48000.00"), "/images/products/beds/ivory_curved_bed.png", beds, "Textured Fabric & Oak", "Denmark", 9, "ivory curved minimal warm organic bed", 4.7));

        productRepository.save(new Product("Royal Purple Cloud Bed",
            "A dramatic purple velvet bed with a lobed headboard and cloud-themed lighting for a whimsical, dreamy atmosphere.",
            new BigDecimal("59000.00"), "/images/products/beds/royal_purple_velvet_bed.png", beds, "Premium Velvet & Beech", "France", 10, "purple royal cloud bed whimsy luxury", 4.9));

        productRepository.save(new Product("Marshmallow Bubble Bed",
            "A plush, tufted cream-colored bed with 'bubble' cushioning and soft glowing wave-like backlighting.",
            new BigDecimal("52000.00"), "/images/products/beds/marshmallow_bubble_bed.png", beds, "Micro-fiber & Pine", "Sweden", 9, "cream bubble marshmallow bed soft chic", 4.8));

        // 3. Core Collection Beds
        productRepository.save(new Product("Emerald Velvet Dream Bed",
            "A luxurious deep green velvet bed with a curved, padded headboard that defines modern luxury.",
            new BigDecimal("45000.00"), "/images/products/beds/emerald_velvet_bed.png", beds, "Emerald Velvet & Pine", "Sweden", 10, "green luxury bedroom bed warm cozy relaxed", 5.0));

        productRepository.save(new Product("Deep Purple Royal Bed",
            "A statement circular bed in rich purple velvet, designed for a dramatic and regal bedroom atmosphere.",
            new BigDecimal("52000.00"), "/images/products/beds/purple_velvet_round_bed.jpg", beds, "Premium Velvet & Oak", "France", 9, "purple royal round bed luxury warm cozy", 4.8));

        productRepository.save(new Product("Starlight Cream Classic Bed",
            "A timeless, elegant bed in soft cream upholstery with a tall tufted headboard and gold accents.",
            new BigDecimal("42000.00"), "/images/products/beds/royal_beige_bed.jpg", beds, "High-grade Linen & Gold-leaf Wood", "Denmark", 10, "cream classic elegant bedroom bright neutral", 4.9));
    }

    private void initializeMoodboardProductLinks() {
        // Stockholm Morning Set
        moodboardRepository.findFirstBySlug("stockholm-morning-set").ifPresent(m -> {
            Set<Product> products = new HashSet<>();
            String[] names = {"Nordic Table Lamp", "Minimal Ceramic Vase", "Oak Coffee Table", "Linen Modular Sofa", "Ash Wood Sculptural Chair", "Minimalist Oak Dresser", "Aesthetic Design Collection", "Indoor Olive Tree", "Nordic Abstract Print"};
            for (String name : names) {
                productRepository.findByName(name).stream().findFirst().ifPresent(products::add);
            }
            if (!products.isEmpty()) {
                m.setProducts(products);
                moodboardRepository.save(m);
            }
        });

        // Nordic Living Sanctuary
        moodboardRepository.findFirstBySlug("nordic-living-sanctuary").ifPresent(m -> {
            Set<Product> products = new HashSet<>();
            String[] names = {"Linen Modular Sofa", "Beige Lounge Chair", "Oak Coffee Table", "Forest Green Velvet Pillow", "Nordic Shawl Collection", "Scandinavian Living Ensemble", "Natural Woven Pillow", "Artisan Textile Bundle"};
            for (String name : names) {
                productRepository.findByName(name).stream().findFirst().ifPresent(products::add);
            }
            if (!products.isEmpty()) {
                m.setProducts(products);
                moodboardRepository.save(m);
            }
        });

        // Nordic Reading Nook
        moodboardRepository.findFirstBySlug("nordic-reading-nook").ifPresent(m -> {
            Set<Product> products = new HashSet<>();
            String[] names = {"Beige Lounge Chair", "Nordic Table Lamp", "Oak Coffee Table", "Minimal Ceramic Vase", "Nordic Abstract Print", "Nordic Reading Ensemble", "Ash Wood Sculptural Chair", "Nordic Shawl Collection", "Natural Woven Pillow"};
            for (String name : names) {
                productRepository.findByName(name).stream().findFirst().ifPresent(products::add);
            }
            if (!products.isEmpty()) {
                m.setProducts(products);
                moodboardRepository.save(m);
            }
        });

        // Olive Living Room
        moodboardRepository.findFirstBySlug("olive-living-room").ifPresent(m -> {
            Set<Product> products = new HashSet<>();
            String[] names = {"Olive Modular Sofa", "Red Accent Pillow", "Beige Cushion Pillow", "Mustard Throw Blanket", "Black Round Coffee Table", "Red Lounge Chair", "Wooden Storage Cabinet", "Woven Wall Decor", "White Pendant Light", "Indoor Olive Tree Plant"};
            for (String name : names) {
                productRepository.findByName(name).stream().findFirst().ifPresent(products::add);
            }
            if (!products.isEmpty()) {
                m.setProducts(products);
                moodboardRepository.save(m);
            }
        });
    }
}

