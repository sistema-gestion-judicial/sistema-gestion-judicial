package mx.edu.utez.judicial.oficialia.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.edu.utez.judicial.oficialia.dao.DemandRequestDao;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;

/**
 * Builds case file numbers with the format YYYY/NNNNN/CC: year, a sequence per court and year, and the court code.
 */
public class CaseFileNumberGenerator {

    private static final Pattern CASE_FILE = Pattern.compile("(\\d{4})/(\\d{5})/(\\w+)");

    private final DemandRequestDao demandRequestDao;

    public CaseFileNumberGenerator(DemandRequestDao demandRequestDao) {
        this.demandRequestDao = demandRequestDao;
    }

    /** Next number for the court in that year. Callers must not run it twice at the same time for one court. */
    public String next(String courtCode, int year) {
        int last = demandRequestDao.findAll().stream()
                .map(DemandRequest::getCaseFileNumber)
                .filter(number -> number != null)
                .map(CASE_FILE::matcher)
                .filter(Matcher::matches)
                .filter(m -> Integer.parseInt(m.group(1)) == year && m.group(3).equals(courtCode))
                .mapToInt(m -> Integer.parseInt(m.group(2)))
                .max()
                .orElse(0);
        return String.format("%d/%05d/%s", year, last + 1, courtCode);
    }
}
