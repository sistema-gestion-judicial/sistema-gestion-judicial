package mx.edu.utez.judicial.oficialia.model;

import java.util.List;

/**
 * Model for a JUZGADO (DFR 2.9). Its two-digit code is the last part of every case file number and never
 * changes; the number is the official court number. Each court belongs to one judicial district and handles
 * one or more subject matters, kept here by their stable codes.
 */
public class Court {

    private final String code;
    private final int number;
    private final String name;
    private final String districtCode;
    private final String districtName;
    private final CourtAddress address;
    private final List<String> subjectMatterCodes;
    private final boolean active;

    public Court(String code, int number, String name, String districtCode, String districtName,
                 CourtAddress address, List<String> subjectMatterCodes, boolean active) {
        this.code = code;
        this.number = number;
        this.name = name;
        this.districtCode = districtCode;
        this.districtName = districtName;
        this.address = address;
        this.subjectMatterCodes = List.copyOf(subjectMatterCodes);
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    /** The district name, which the API has published as judicialDistrict since #29. */
    public String getJudicialDistrict() {
        return districtName;
    }

    public CourtAddress getAddress() {
        return address;
    }

    public List<String> getSubjectMatterCodes() {
        return subjectMatterCodes;
    }

    public boolean isActive() {
        return active;
    }
}
