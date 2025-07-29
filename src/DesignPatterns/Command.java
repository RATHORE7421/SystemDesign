package src.DesignPatterns;

import java.util.Stack;

// Receiver – TextDocument
class TextDocument {
    private StringBuilder content = new StringBuilder();

    public void append(String text) {
        content.append(text);
    }

    public void removeLast(int length) {
        if (length > content.length()) length = content.length();
        content.delete(content.length() - length, content.length());
    }

    public String getContent() {
        return content.toString();
    }
}

// Command Interface with Undo
interface CommandInt {
    void execute();
    void undo();
}

// Concrete Command – AppendTextCommand
class AppendTextCommand implements CommandInt {
    private TextDocument document;
    private String text;

    public AppendTextCommand(TextDocument document, String text) {
        this.document = document;
        this.text = text;
    }

    public void execute() {
        document.append(text);
    }

    public void undo() {
        document.removeLast(text.length());
    }
}

// Invoker – Editor (Handles history)
class Editor {
    private Stack<CommandInt> history = new Stack<>();
    private Stack<CommandInt> redoStack = new Stack<>();

    public void executeCommand(CommandInt command) {
        command.execute();
        history.push(command);
        redoStack.clear();  // Clear redo on new action
    }

    public void undo() {
        if (!history.isEmpty()) {
            CommandInt last = history.pop();
            last.undo();
            redoStack.push(last);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            CommandInt command = redoStack.pop();
            command.execute();
            history.push(command);
        }
    }
}

// Client Code – Test It!
public class Command {
    public static void main(String[] args) {
        TextDocument document = new TextDocument();
        Editor editor = new Editor();

        CommandInt cmd1 = new AppendTextCommand(document, "Hello ");
        CommandInt cmd2 = new AppendTextCommand(document, "Priya!");
        CommandInt cmd3 = new AppendTextCommand(document, " ❤️");

        editor.executeCommand(cmd1);
        editor.executeCommand(cmd2);
        editor.executeCommand(cmd3);

        System.out.println("After typing: " + document.getContent());

        editor.undo();
        System.out.println("After undo: " + document.getContent());

        editor.redo();
        System.out.println("After redo: " + document.getContent());

        editor.undo();
        editor.undo();
        System.out.println("After 2 more undo: " + document.getContent());

        editor.redo();
        System.out.println("After 1 redo: " + document.getContent());
    }
}

// Output
// After typing: Hello Priya! ❤️  
// After undo: Hello Priya!  
// After redo: Hello Priya! ❤️  
// After 2 more undo: Hello  
// After 1 redo: Hello Priya!


