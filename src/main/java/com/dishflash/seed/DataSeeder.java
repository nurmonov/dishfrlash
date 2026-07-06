package com.dishflash.seed;

import com.dishflash.crypto.CryptoService;
import com.dishflash.entity.*;
import com.dishflash.enums.OrderStatus;
import com.dishflash.enums.PaymentStatus;
import com.dishflash.enums.Role;
import com.dishflash.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder {

    private static final String C_FULL_NAME = "users:full_name";
    private static final String C_PHONE = "users:phone";
    private static final String C_EMAIL = "users:email";
    private static final String C_BIRTH_DATE = "users:birth_date";
    private static final String C_ROLE = "users:role";
    private static final String C_USER_CREATED_AT = "users:created_at";
    private static final String C_REGION = "addresses:region";
    private static final String C_CITY = "addresses:city";
    private static final String C_STREET = "addresses:street";
    private static final String C_HOUSE = "addresses:house_apartment";
    private static final String C_POSTAL_CODE = "addresses:postal_code";
    private static final String C_DELIVERY = "orders:delivery_address";
    private static final String C_RECIPIENT = "orders:recipient_phone";
    private static final String C_ORDER_STATUS = "orders:status";
    private static final String C_ORDER_TOTAL = "orders:total_price";
    private static final String C_ORDER_CREATED_AT = "orders:created_at";
    private static final String C_CARD_NUM = "payments:card_number";
    private static final String C_CARD_HOLDER = "payments:card_holder";
    private static final String C_PAY_AMOUNT = "payments:amount";
    private static final String C_PAY_STATUS = "payments:status";
    private static final String C_PAY_CREATED_AT = "payments:created_at";
    private static final String C_IP = "audit:ip_address";
    private static final String C_AUDIT_ACTION = "audit:action";
    private static final String C_AUDIT_ENTITY = "audit:entity_name";
    private static final String C_AUDIT_TIMESTAMP = "audit:timestamp";
    private static final String C_CAT_NAME = "categories:name";
    private static final String C_PROD_TITLE = "products:title";
    private static final String C_PROD_DESC = "products:description";
    private static final String C_PROD_PRICE = "products:price";
    private static final String C_PROD_STOCK = "products:stock_quantity";
    private static final String C_PROD_RATING = "products:rating";
    private static final String C_PROD_CREATED_AT = "products:created_at";
    private static final String C_REVIEW_RATING = "reviews:rating";
    private static final String C_REVIEW_COMMENT = "reviews:comment";
    private static final String C_REVIEW_CREATED_AT = "reviews:created_at";
    private static final String C_ITEM_QTY = "order_items:quantity";
    private static final String C_ITEM_PRICE = "order_items:price_at_order";

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryNameNgramRepository catNgramRepository;
    private final ProductRepository productRepository;
    private final ProductTitleNgramRepository prodNgramRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewCommentNgramRepository reviewNgramRepository;
    private final UserNameNgramRepository userNameNgramRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditActionNgramRepository auditActionNgramRepository;
    private final AuditEntityNameNgramRepository auditEntityNgramRepository;
    private final CryptoService crypto;
    private final PasswordEncoder passwordEncoder;

    private static final String[] MALE = {
            "Abdulla", "Akbar", "Akmal", "Alisher", "Aziz", "Bahrom", "Bakhtiyor", "Botir",
            "Dilshod", "Eldor", "Farrukh", "Furqat", "Hamza", "Ilhom", "Jamshid", "Jasur",
            "Kamol", "Laziz", "Murod", "Nodir", "Olim", "Otabek", "Ravshan", "Rustam",
            "Sardor", "Sherali", "Sherzod", "Shuhrat", "Sunnat", "Temur", "Ulugbek",
            "Umid", "Xurshid", "Yusuf", "Zafar", "Zohid"
    };
    private static final String[] FEMALE = {
            "Adolat", "Barno", "Dilfuza", "Dildora", "Feruza", "Gulbahor", "Gulnora",
            "Hilola", "Iroda", "Kamola", "Lola", "Madina", "Mahbuba", "Malika",
            "Nasiba", "Nigora", "Nodira", "Robiya", "Sabina", "Saida", "Sevara",
            "Shahlo", "Shirin", "Sitora", "Sofiya", "Umida", "Yulduz", "Zebo"
    };
    private static final String[] LAST = {
            "Abdullayev", "Ahmadov", "Alimov", "Azizov", "Ergashev", "Fayzullayev",
            "Ganiyev", "Hakimov", "Ibrahimov", "Ismailov", "Jalilov", "Kamolov",
            "Karimov", "Komilov", "Murodov", "Nazarov", "Normatov", "Nurmatov",
            "Ochilov", "Qodirov", "Rahimov", "Rasulov", "Ruziyev", "Safarov",
            "Sultonov", "Tursunov", "Umarov", "Usmanov", "Xalilov", "Zokirov"
    };
    private static final String[] CATS = {
            "Elektronika", "Smartfonlar", "Noutbuklar", "Planshetlar", "Quloqchinlar",
            "Maishiy texnika", "Sovutgichlar", "Kir yuvish", "Changyutgichlar",
            "Kiyim-kechak", "Erkaklar", "Ayollar", "Bolalar", "Poyabzallar",
            "Sport", "Kitoblar", "Uy-ro'zg'or", "Mebel", "Oshxona", "Go'zallik",
            "Parfyumeriya", "Kosmetika", "Avtotovarlar", "Bog' jihozlari",
            "O'yinchoqlar", "Kantselyariya", "Sog'liqni saqlash", "Zargarlik",
            "Soatlar", "Sumkalar"
    };
    private static final String[] REGIONS = {
            "Toshkent shahri", "Toshkent viloyati", "Samarqand", "Buxoro",
            "Farg'ona", "Namangan", "Andijon", "Qashqadaryo", "Surxondaryo",
            "Navoiy", "Jizzax", "Xorazm", "Qoraqalpog'iston"
    };
    private static final String[] CITIES = {
            "Toshkent", "Samarqand", "Buxoro", "Farg'ona", "Namangan",
            "Andijon", "Qarshi", "Termiz", "Navoiy", "Jizzax", "Urganch", "Nukus"
    };
    private static final String[] STREETS = {
            "Amir Temur", "Navoiy", "Bunyodkor", "Buyuk Turon", "Mirzo Ulug'bek",
            "Bobur", "Beruniy", "Ibn Sino", "Chilonzor", "Yunusabad", "Mirobod"
    };

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping...");
            return;
        }

        long start = System.currentTimeMillis();
        log.info("=== SEED START ===");

        List<Category> cats = seedCategories();
        log.info("Categories: {}", cats.size());

        List<User> users = seedUsers();
        log.info("Users: {}", users.size());

        List<User> sellers = users.stream().filter(u -> {
            byte[] roleDec = crypto.decrypt(u.getRoleEncrypted(), C_ROLE);
            return roleDec != null && new String(roleDec, StandardCharsets.UTF_8).equals("SELLER");
        }).toList();
        List<Product> products = seedProducts(sellers, cats);
        log.info("Products: {}", products.size());

        seedAddresses(users);
        seedOrdersAndPayments(users, products);
        seedReviews(users, products);
        seedAuditLogs(users);

        log.info("=== SEED DONE in {}s ===", (System.currentTimeMillis() - start) / 1000);
    }

    private List<Category> seedCategories() {
        List<Category> list = new ArrayList<>();
        for (String name : CATS) {
            Category cat = Category.builder()
                    .nameEncrypted(crypto.encrypt(name.getBytes(StandardCharsets.UTF_8), C_CAT_NAME))
                    .nameBlindIndex(crypto.blindIndex(name, C_CAT_NAME))
                    .build();
            list.add(categoryRepository.save(cat));
        }
        link("Smartfonlar", "Elektronika"); link("Noutbuklar", "Elektronika");
        link("Planshetlar", "Elektronika"); link("Quloqchinlar", "Elektronika");
        link("Sovutgichlar", "Maishiy texnika"); link("Kir yuvish", "Maishiy texnika");
        link("Changyutgichlar", "Maishiy texnika");
        link("Erkaklar", "Kiyim-kechak"); link("Ayollar", "Kiyim-kechak");
        link("Bolalar", "Kiyim-kechak");
        list = categoryRepository.findAll();
        saveCatNgrams(list);
        return list;
    }

    private void saveCatNgrams(List<Category> cats) {
        List<CategoryNameNgram> all = new ArrayList<>();
        for (Category c : cats) {
            byte[] dec = crypto.decrypt(c.getNameEncrypted(), C_CAT_NAME);
            if (dec != null) {
                String name = new String(dec, StandardCharsets.UTF_8);
                List<String> idxs = crypto.generateNgramBlindIndices(name, C_CAT_NAME);
                for (String idx : idxs) {
                    all.add(CategoryNameNgram.builder().category(c).ngramBlindIndex(idx).build());
                }
            }
        }
        catNgramRepository.saveAll(all);
    }

    private void link(String child, String parent) {
        byte[] parentDec = null;
        byte[] childDec = null;
        Category parentCat = null, childCat = null;
        for (Category c : categoryRepository.findAll()) {
            byte[] dec = crypto.decrypt(c.getNameEncrypted(), C_CAT_NAME);
            if (dec != null) {
                String name = new String(dec, StandardCharsets.UTF_8);
                if (name.equals(parent)) { parentCat = c; parentDec = dec; }
                if (name.equals(child)) { childCat = c; childDec = dec; }
            }
        }
        if (childCat != null && parentCat != null) {
            childCat.setParent(parentCat);
            categoryRepository.save(childCat);
        }
    }

    private List<User> seedUsers() {
        List<User> users = new ArrayList<>();
        Set<String> usedPhones = new HashSet<>();
        Set<String> usedEmails = new HashSet<>();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String now = LocalDateTime.now().toString();

        for (int i = 0; i < 1000; i++) {
            boolean seller = i < 100;
            String firstName = r.nextBoolean() ? MALE[r.nextInt(MALE.length)] : FEMALE[r.nextInt(FEMALE.length)];
            String lastName = LAST[r.nextInt(LAST.length)];
            String fullName = firstName + " " + lastName;

            String phone;
            do { phone = "+998" + String.format("%02d%07d", r.nextInt(10, 99), r.nextInt(10_000_000)); }
            while (usedPhones.contains(phone));
            usedPhones.add(phone);

            String email;
            do { email = firstName.toLowerCase() + "." + lastName.toLowerCase() + r.nextInt(9999) + "@gmail.com"; }
            while (usedEmails.contains(email));
            usedEmails.add(email);

            String birthDate = String.format("%04d-%02d-%02d", r.nextInt(1960, 2005), r.nextInt(1, 13), r.nextInt(1, 28));
            String role = seller ? "SELLER" : "CUSTOMER";

            User user = User.builder()
                    .fullNameEncrypted(crypto.encrypt(fullName.getBytes(StandardCharsets.UTF_8), C_FULL_NAME))
                    .fullNameBlindIndex(crypto.blindIndex(fullName, C_FULL_NAME))
                    .phoneEncrypted(crypto.encrypt(phone.getBytes(StandardCharsets.UTF_8), C_PHONE))
                    .phoneBlindIndex(crypto.blindIndex(phone, C_PHONE))
                    .emailEncrypted(crypto.encrypt(email.getBytes(StandardCharsets.UTF_8), C_EMAIL))
                    .emailBlindIndex(crypto.blindIndex(email, C_EMAIL))
                    .passwordHash(passwordEncoder.encode("password123"))
                    .birthDateEncrypted(crypto.encrypt(birthDate.getBytes(StandardCharsets.UTF_8), C_BIRTH_DATE))
                    .roleEncrypted(crypto.encrypt(role.getBytes(StandardCharsets.UTF_8), C_ROLE))
                    .roleBlindIndex(crypto.blindIndex(role, C_ROLE))
                    .createdAtEncrypted(crypto.encrypt(now.getBytes(StandardCharsets.UTF_8), C_USER_CREATED_AT))
                    .build();
            users.add(user);
        }

        users = userRepository.saveAll(users);

        List<UserNameNgram> allNgrams = new ArrayList<>();
        for (User u : users) {
            byte[] dec = crypto.decrypt(u.getFullNameEncrypted(), C_FULL_NAME);
            if (dec != null) {
                String name = new String(dec, StandardCharsets.UTF_8);
                List<String> indices = crypto.generateNgramBlindIndices(name, C_FULL_NAME);
                for (String idx : indices) {
                    allNgrams.add(UserNameNgram.builder().user(u).ngramBlindIndex(idx).build());
                }
            }
        }
        userNameNgramRepository.saveAll(allNgrams);

        return users;
    }

    private List<Product> seedProducts(List<User> sellers, List<Category> categories) {
        List<Product> all = new ArrayList<>();
        List<ProductTitleNgram> allNgrams = new ArrayList<>();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        List<Category> leafCats = new ArrayList<>();
        for (Category c : categories) {
            byte[] dec = crypto.decrypt(c.getNameEncrypted(), C_CAT_NAME);
            if (dec != null) {
                String name = new String(dec, StandardCharsets.UTF_8);
                if (!List.of("Elektronika", "Maishiy texnika", "Kiyim-kechak").contains(name)) {
                    leafCats.add(c);
                }
            }
        }

        for (int i = 0; i < 5000; i++) {
            Category cat = leafCats.get(r.nextInt(leafCats.size()));
            User seller = sellers.get(r.nextInt(sellers.size()));
            double price = r.nextDouble(10000, 15000000);
            String title = cat.getNameEncrypted() != null ? "Mahsulot " + (i + 1) : "Product " + (i + 1);
            String desc = "Sifatli mahsulot, " + r.nextInt(1, 5) + " yil kafolat";
            int stock = r.nextInt(1, 500);
            double rating = Math.round(r.nextDouble(3.0, 5.0) * 10) / 10.0;

            Product p = Product.builder()
                    .titleEncrypted(crypto.encrypt(title.getBytes(StandardCharsets.UTF_8), C_PROD_TITLE))
                    .titleBlindIndex(crypto.blindIndex(title, C_PROD_TITLE))
                    .descriptionEncrypted(crypto.encrypt(desc.getBytes(StandardCharsets.UTF_8), C_PROD_DESC))
                    .priceEncrypted(crypto.encrypt(String.valueOf(price).getBytes(StandardCharsets.UTF_8), C_PROD_PRICE))
                    .stockQuantityEncrypted(crypto.encrypt(String.valueOf(stock).getBytes(StandardCharsets.UTF_8), C_PROD_STOCK))
                    .ratingEncrypted(crypto.encrypt(String.valueOf(rating).getBytes(StandardCharsets.UTF_8), C_PROD_RATING))
                    .category(cat)
                    .seller(seller)
                    .createdAtEncrypted(crypto.encrypt(LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8), C_PROD_CREATED_AT))
                    .build();
            all.add(p);

            List<String> ngrams = crypto.generateNgramBlindIndices(title, C_PROD_TITLE);
            for (String idx : ngrams) {
                allNgrams.add(ProductTitleNgram.builder().product(p).ngramBlindIndex(idx).build());
            }

            if (all.size() >= 1000) {
                productRepository.saveAll(all);
                prodNgramRepository.saveAll(allNgrams);
                all.clear(); allNgrams.clear();
            }
        }
        if (!all.isEmpty()) {
            productRepository.saveAll(all);
            prodNgramRepository.saveAll(allNgrams);
        }
        return productRepository.findAll();
    }

    private void seedAddresses(List<User> users) {
        List<Address> all = new ArrayList<>();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (User u : users) {
            for (int a = 0, n = r.nextInt(1, 3); a < n; a++) {
                String region = REGIONS[r.nextInt(REGIONS.length)];
                String city = CITIES[r.nextInt(CITIES.length)];
                String street = STREETS[r.nextInt(STREETS.length)] + " ko'chasi";
                String house = r.nextInt(1, 200) + "-" + r.nextInt(1, 100);
                String zip = String.format("%06d", r.nextInt(100000, 999999));
                all.add(Address.builder()
                        .user(u)
                        .regionEncrypted(crypto.encrypt(region.getBytes(StandardCharsets.UTF_8), C_REGION))
                        .regionBlindIndex(crypto.blindIndex(region, C_REGION))
                        .cityEncrypted(crypto.encrypt(city.getBytes(StandardCharsets.UTF_8), C_CITY))
                        .cityBlindIndex(crypto.blindIndex(city, C_CITY))
                        .streetEncrypted(crypto.encrypt(street.getBytes(StandardCharsets.UTF_8), C_STREET))
                        .houseApartmentEncrypted(crypto.encrypt(house.getBytes(StandardCharsets.UTF_8), C_HOUSE))
                        .postalCodeEncrypted(crypto.encrypt(zip.getBytes(StandardCharsets.UTF_8), C_POSTAL_CODE))
                        .postalCodeBlindIndex(crypto.blindIndex(zip, C_POSTAL_CODE))
                        .build());
            }
            if (all.size() >= 1000) { addressRepository.saveAll(all); all.clear(); }
        }
        if (!all.isEmpty()) addressRepository.saveAll(all);
    }

    private void seedOrdersAndPayments(List<User> users, List<Product> products) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int total = 30000;
        int totalUsers = users.size();
        int powerCount = totalUsers * 20 / 100;

        List<Long> orderUserIds = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            if (i < total * 80 / 100) {
                orderUserIds.add(users.get(r.nextInt(powerCount)).getId());
            } else {
                orderUserIds.add(users.get(powerCount + r.nextInt(totalUsers - powerCount)).getId());
            }
        }
        Collections.shuffle(orderUserIds, new Random(r.nextLong()));

        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        List<Order> orders = new ArrayList<>();
        List<OrderItem> items = new ArrayList<>();
        List<Payment> payments = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            User u = userMap.get(orderUserIds.get(i));
            String addr = REGIONS[r.nextInt(REGIONS.length)] + ", " + STREETS[r.nextInt(STREETS.length)] + " ko'chasi";
            String phone = "+998" + String.format("%02d%07d", r.nextInt(10, 99), r.nextInt(10_000_000));
            String status = OrderStatus.values()[r.nextInt(OrderStatus.values().length)].name();

            Order o = Order.builder()
                    .user(u)
                    .statusEncrypted(crypto.encrypt(status.getBytes(StandardCharsets.UTF_8), C_ORDER_STATUS))
                    .statusBlindIndex(crypto.blindIndex(status, C_ORDER_STATUS))
                    .totalPriceEncrypted(crypto.encrypt("0".getBytes(StandardCharsets.UTF_8), C_ORDER_TOTAL))
                    .deliveryAddressEncrypted(crypto.encrypt(addr.getBytes(StandardCharsets.UTF_8), C_DELIVERY))
                    .recipientPhoneEncrypted(crypto.encrypt(phone.getBytes(StandardCharsets.UTF_8), C_RECIPIENT))
                    .recipientPhoneBlindIndex(crypto.blindIndex(phone, C_RECIPIENT))
                    .createdAtEncrypted(crypto.encrypt(LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8), C_ORDER_CREATED_AT))
                    .build();

            BigDecimal totalPrice = BigDecimal.ZERO;
            for (int j = 0, n = r.nextInt(1, 6); j < n; j++) {
                Product p = products.get(r.nextInt(products.size()));
                int qty = r.nextInt(1, 4);
                byte[] priceDec = crypto.decrypt(p.getPriceEncrypted(), C_PROD_PRICE);
                BigDecimal price = new BigDecimal(new String(priceDec, StandardCharsets.UTF_8));
                BigDecimal sub = price.multiply(BigDecimal.valueOf(qty));
                totalPrice = totalPrice.add(sub);
                items.add(OrderItem.builder()
                        .order(o)
                        .product(p)
                        .quantityEncrypted(crypto.encrypt(String.valueOf(qty).getBytes(StandardCharsets.UTF_8), C_ITEM_QTY))
                        .priceAtOrderEncrypted(crypto.encrypt(price.toPlainString().getBytes(StandardCharsets.UTF_8), C_ITEM_PRICE))
                        .build());
            }
            o.setTotalPriceEncrypted(crypto.encrypt(totalPrice.toPlainString().getBytes(StandardCharsets.UTF_8), C_ORDER_TOTAL));
            orders.add(o);

            String cardNum = "9860" + String.format("%010d", r.nextLong(1_000_000_000_0L));
            String last4 = cardNum.substring(cardNum.length() - 4);
            payments.add(Payment.builder()
                    .order(o)
                    .amountEncrypted(crypto.encrypt(totalPrice.toPlainString().getBytes(StandardCharsets.UTF_8), C_PAY_AMOUNT))
                    .statusEncrypted(crypto.encrypt(PaymentStatus.SUCCESS.name().getBytes(StandardCharsets.UTF_8), C_PAY_STATUS))
                    .statusBlindIndex(crypto.blindIndex(PaymentStatus.SUCCESS.name(), C_PAY_STATUS))
                    .cardNumberEncrypted(crypto.encrypt(cardNum.getBytes(StandardCharsets.UTF_8), C_CARD_NUM))
                    .cardLast4BlindIndex(crypto.blindIndex(last4, C_CARD_NUM))
                    .cardHolderNameEncrypted(crypto.encrypt("HOLDER".getBytes(StandardCharsets.UTF_8), C_CARD_HOLDER))
                    .createdAtEncrypted(crypto.encrypt(LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8), C_PAY_CREATED_AT))
                    .build());

            if (orders.size() >= 1000) {
                orderRepository.saveAll(orders); orderItemRepository.saveAll(items); paymentRepository.saveAll(payments);
                orders.clear(); items.clear(); payments.clear();
            }
        }
        if (!orders.isEmpty()) {
            orderRepository.saveAll(orders); orderItemRepository.saveAll(items); paymentRepository.saveAll(payments);
        }
    }

    private void seedReviews(List<User> users, List<Product> products) {
        List<Review> all = new ArrayList<>();
        List<ReviewCommentNgram> allNgrams = new ArrayList<>();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String[] comments = {"Ajoyib!", "Yaxshi mahsulot", "Qoniqdim", "Tavsiya qilaman", "Tez yetkazildi", "Narxiga arziydi"};
        for (int i = 0; i < 15000; i++) {
            String comment = comments[r.nextInt(comments.length)];
            int rating = r.nextInt(3, 6);
            Review rev = Review.builder()
                    .product(products.get(r.nextInt(products.size())))
                    .user(users.get(r.nextInt(users.size())))
                    .ratingEncrypted(crypto.encrypt(String.valueOf(rating).getBytes(StandardCharsets.UTF_8), C_REVIEW_RATING))
                    .commentEncrypted(crypto.encrypt(comment.getBytes(StandardCharsets.UTF_8), C_REVIEW_COMMENT))
                    .createdAtEncrypted(crypto.encrypt(LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8), C_REVIEW_CREATED_AT))
                    .build();
            all.add(rev);

            List<String> ngrams = crypto.generateNgramBlindIndices(comment, C_REVIEW_COMMENT);
            for (String idx : ngrams) {
                allNgrams.add(ReviewCommentNgram.builder().review(rev).ngramBlindIndex(idx).build());
            }

            if (all.size() >= 1000) {
                reviewRepository.saveAll(all); reviewNgramRepository.saveAll(allNgrams);
                all.clear(); allNgrams.clear();
            }
        }
        if (!all.isEmpty()) {
            reviewRepository.saveAll(all); reviewNgramRepository.saveAll(allNgrams);
        }
    }

    private void seedAuditLogs(List<User> users) {
        List<AuditLog> all = new ArrayList<>();
        List<AuditActionNgram> actionNgrams = new ArrayList<>();
        List<AuditEntityNameNgram> entityNgrams = new ArrayList<>();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String[] actions = {"USER_LOGIN", "ORDER_CREATED", "PRODUCT_SEARCH", "PAYMENT_PROCESSED"};
        String now = LocalDateTime.now().toString();
        for (int i = 0; i < 2000; i++) {
            String action = actions[r.nextInt(actions.length)];
            String entityName = "Entity";
            AuditLog al = AuditLog.builder()
                    .user(users.get(r.nextInt(users.size())))
                    .actionEncrypted(crypto.encrypt(action.getBytes(StandardCharsets.UTF_8), C_AUDIT_ACTION))
                    .actionBlindIndex(crypto.blindIndex(action, C_AUDIT_ACTION))
                    .entityNameEncrypted(crypto.encrypt(entityName.getBytes(StandardCharsets.UTF_8), C_AUDIT_ENTITY))
                    .entityNameBlindIndex(crypto.blindIndex(entityName, C_AUDIT_ENTITY))
                    .entityId((long) r.nextInt(1, 10000))
                    .ipAddressEncrypted(crypto.encrypt(
                            ("192.168." + r.nextInt(256) + "." + r.nextInt(256)).getBytes(StandardCharsets.UTF_8), C_IP))
                    .timestampEncrypted(crypto.encrypt(now.getBytes(StandardCharsets.UTF_8), C_AUDIT_TIMESTAMP))
                    .build();
            all.add(al);

            List<String> actNgrams = crypto.generateNgramBlindIndices(action, C_AUDIT_ACTION);
            for (String idx : actNgrams) {
                actionNgrams.add(AuditActionNgram.builder().auditLog(al).ngramBlindIndex(idx).build());
            }
            List<String> entNgrams = crypto.generateNgramBlindIndices(entityName, C_AUDIT_ENTITY);
            for (String idx : entNgrams) {
                entityNgrams.add(AuditEntityNameNgram.builder().auditLog(al).ngramBlindIndex(idx).build());
            }

            if (all.size() >= 500) {
                auditLogRepository.saveAll(all); auditActionNgramRepository.saveAll(actionNgrams);
                auditEntityNgramRepository.saveAll(entityNgrams);
                all.clear(); actionNgrams.clear(); entityNgrams.clear();
            }
        }
        if (!all.isEmpty()) {
            auditLogRepository.saveAll(all); auditActionNgramRepository.saveAll(actionNgrams);
            auditEntityNgramRepository.saveAll(entityNgrams);
        }
    }
}
