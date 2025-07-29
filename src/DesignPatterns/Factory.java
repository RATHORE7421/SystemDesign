package src.DesignPatterns;

interface Shape {
    void draw();
}

class Circle implements Shape {
    public void draw() {
        System.out.println("Drawing a Circle");
    }
}

class Square implements Shape {
    public void draw() {
        System.out.println("Drawing a Square");
    }
}

class Triangle implements Shape {
    public void draw() {
        System.out.println("Drawing a Triangle");
    }
}

class ShapeFactory {
    public Shape getShape(String shapeType) {
        if (shapeType == null) return null;
        switch (shapeType.toLowerCase()) {
            case "circle": return new Circle();
            case "square": return new Square();
            case "triangle": return new Triangle();
            default: return null;
        }
    }
}

public class Factory {
    public static void main(String[] args) {
        ShapeFactory factory = new ShapeFactory();

        Shape s1 = factory.getShape("circle");
        s1.draw();  // Drawing a Circle

        Shape s2 = factory.getShape("square");
        s2.draw();  // Drawing a Square
    }
}

