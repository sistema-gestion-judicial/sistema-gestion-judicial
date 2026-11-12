package mx.edu.utez.judicial.oficialia.model;

import java.util.List;

/**
 * What an update changes in a court (DFR 2.11): its name, district, address and the subject matters it handles.
 * The code and the number are not here because they cannot change.
 */
public record CourtChanges(String name, String districtCode, CourtAddress address, List<String> subjectMatterCodes) {

    public CourtChanges {
        subjectMatterCodes = List.copyOf(subjectMatterCodes);
    }
}
