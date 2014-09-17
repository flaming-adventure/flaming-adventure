package no.flaming_adventure.db_admin;

import org.apache.commons.cli.*;

public class Cli {
    public static CommandLine parse(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();

        Options options = new Options();
        options.addOption("f", "file", true, "path to the database file");
        options.addOption("r", "reset", false, "reset the database");
        options.addOption("h", "help", false, "print this message");

        CommandLine line = parser.parse( options, args );

        if (true) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "db_admin", options );
        }

        return line;
    }
}

