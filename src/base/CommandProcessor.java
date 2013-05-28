package base;

public interface CommandProcessor {
    /**
     * Process a new command
     * @param command String
     */
    public void processCommand(Console c, String command);

    /**
     * Get prompt to display to left of user commands; i.e. ">"
     * @return String
     */
    public String getPrompt();

    public CommandHistory getCommandHistory();

}
