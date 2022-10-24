public enum Category {
    EDA("магазин", "питание", "еда", "продукты", "покушать"),
    SWEET("сладкое", "конфеты", "круассан", "ягоды", "фрукты"),
    CAFE("кафе", "ресторан", "пицца", "суши", "обед"),
    FUN("парк", "центр", "развлечение", "вечеринка", "прогулка"),
    FUEL("заправка", "бензин", "дизель", "пежо", "киа"),
    TAXES("школа", "квартира", "садик", "телефон", "оплата"),
    TOYS("подарок", "игрушка", "книжка", "игрушки", "детям"),
    COSMETICS("косметика", "шампунь", "крем", "уход", "маска"),
    CLOTHES("одежда", "обувь", "ботинки", "одежду", "шапка"),
    HEALTH("врач", "массаж", "лекарства", "косультация", "прием"),
    OTHER("презент", "ty", "ty", "ty", "ty");

    String anotherName, secondName, thirdname, fourthname, fifthname;

    Category() {    }

    Category(String anotherName) {
        this.anotherName = anotherName;
    }

    Category(String anotherName, String secondName) {
        this.anotherName = anotherName;
        this.secondName = secondName;
    }

    Category(String anotherName, String secondName, String thirdname) {
        this.anotherName = anotherName;
        this.secondName = secondName;
        this.thirdname = thirdname;
    }

    Category(String anotherName, String secondName, String thirdname, String fourthname) {
        this.anotherName = anotherName;
        this.secondName = secondName;
        this.thirdname = thirdname;
        this.fourthname = fourthname;
    }

    Category(String anotherName, String secondName, String thirdname, String fourthname, String fifthname) {
        this.anotherName = anotherName;
        this.secondName = secondName;
        this.thirdname = thirdname;
        this.fourthname = fourthname;
        this.fifthname = fifthname;
    }

    public static Category fromValue(String value) {
        for (final Category category : values()) {

            if (category.anotherName.equalsIgnoreCase(value)) {
                return category;
            }
            if (category.secondName.equalsIgnoreCase(value)) {
                return category;
            }
            if (category.thirdname.equalsIgnoreCase(value)) {
                return category;
            }

            if (category.fourthname.equalsIgnoreCase(value)) {
                return category;
            }

            if (category.fifthname.equalsIgnoreCase(value)) {
                return category;
            }
        }
        return null;
    }

    private static String enumToString(Category cat) {
        return cat.toString().toLowerCase();
    }

    public static String findCategory(String string) {
        string.replace("  ", "");
        for (String part : string.split(" ")) {
            if (Category.fromValue(part) != null) {
                return enumToString(Category.fromValue(part));
            }
        }
        return enumToString(Category.OTHER);
    }

    public static String[] keyWords() {
        String[] keywodsForCategory = new String[Category.values().length];
        int i = 0;
        for (Category category : Category.values()) {
            keywodsForCategory[i] = category.name() + ": " + category.anotherName + ", " + category.secondName + ", "
                    + category.thirdname + ", " + category.fourthname + ", " + category.fifthname;
            i++;
        }
        return keywodsForCategory;
    }
}
