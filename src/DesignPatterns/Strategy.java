package src.DesignPatterns;

interface TextFormatterStrategy {
    public String format(String s);
}

class LowerCaseFormatter implements TextFormatterStrategy{

    public String format(String s) {
        System.out.println("I am here");
        System.out.println("String " + s.toLowerCase());
        return s.toLowerCase();
    }
}

class UpperCaseFormatter implements TextFormatterStrategy{
    public String format(String s) {
        return s.toUpperCase();
    }
}

class TitleCaseFormatter implements TextFormatterStrategy {
    public String format(String s) {
        StringBuilder str = new StringBuilder();
        int i = 0;
        for(char c : s.toCharArray()) {
            if(i==0 || s.charAt(i-1) == ' ')
            {
                str.append(Character.toUpperCase(c));
            } else {
                str.append(Character.toLowerCase(c));
            }
            i++;
        }
        return str.toString();
    }
}

class DecideTextFormatting {
    public TextFormatterStrategy str;

    public TextFormatterStrategy setStrategy(TextFormatterStrategy str) {
        return this.str = str;
    }

    public String modify(String s) {
        return str.format(s);
    }
}

public class Strategy {
    public static void main(String[] args) {
        DecideTextFormatting textFormat = new DecideTextFormatting();
        textFormat.setStrategy(new TitleCaseFormatter());             
        String s = "   helLo priya, welcome to strategy pattern!";
        System.out.println(textFormat.modify(s));
    }
}
