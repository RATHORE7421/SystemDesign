package src.DesignPatterns;

// class Burger {
//     private String bun;
//     private String patty;
//     private boolean cheese;
//     private boolean lettuce;

//     private Burger(Builder builder) {
//         this.bun = builder.bun;
//         this.patty = builder.patty;
//         this.cheese = builder.cheese;
//         this.lettuce = builder.lettuce;
//     }

//     public void display() {
//         System.out.println("Burger with " + patty + " patty, " + 
//             (cheese ? "cheese, " : "") +
//             (lettuce ? "lettuce, " : "") +
//             "on " + bun + " bun.");
//     }

//     // 🧱 Inner static Builder class
//     public static class Builder {
//         private String bun;
//         private String patty;
//         private boolean cheese;
//         private boolean lettuce;

//         public Builder setBun(String bun) {
//             this.bun = bun;
//             return this;
//         }

//         public Builder setPatty(String patty) {
//             this.patty = patty;
//             return this;
//         }

//         public Builder addCheese(boolean cheese) {
//             this.cheese = cheese;
//             return this;
//         }

//         public Builder addLettuce(boolean lettuce) {
//             this.lettuce = lettuce;
//             return this;
//         }

//         public Burger build() {
//             return new Burger(this);
//         }
//     }
// }

class Burger {
    private String bun;
    private String patty;
    private boolean cheese;
    private boolean lettuce;

    // Constructor should be package-private or public
    public Burger(BurgerBuilder builder) {
        this.bun = builder.bun;
        this.patty = builder.patty;
        this.cheese = builder.cheese;
        this.lettuce = builder.lettuce;
    }

    public void display() {
        System.out.println("Burger with " + patty + " patty, " + 
            (cheese ? "cheese, " : "") +
            (lettuce ? "lettuce, " : "") +
            "on " + bun + " bun.");
    }
}


class BurgerBuilder {
    String bun;
    String patty;
    boolean cheese;
    boolean lettuce;

    public BurgerBuilder setBun(String bun) {
        this.bun = bun;
        return this;
    }

    public BurgerBuilder setPatty(String patty) {
        this.patty = patty;
        return this;
    }

    public BurgerBuilder addCheese(boolean cheese) {
        this.cheese = cheese;
        return this;
    }

    public BurgerBuilder addLettuce(boolean lettuce) {
        this.lettuce = lettuce;
        return this;
    }

    public Burger build() {
        return new Burger(this);
    }
}


public class Builder {
    public static void main(String[] args) {
        Burger burger = new BurgerBuilder()
            .setBun("Wheat")
            .setPatty("Chicken")
            .addLettuce(true)
            .build();

        burger.display();
    }
}
    
