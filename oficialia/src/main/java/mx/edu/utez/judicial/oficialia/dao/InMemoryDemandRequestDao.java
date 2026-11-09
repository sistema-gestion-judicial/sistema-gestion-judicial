package mx.edu.utez.judicial.oficialia.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import mx.edu.utez.judicial.oficialia.model.Annex;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityWarning;
import mx.edu.utez.judicial.oficialia.model.Party;
import mx.edu.utez.judicial.oficialia.model.PartyRole;

/**
 * Keeps the demand requests in memory with sample data, shared by every servlet of the application.
 * Changes last until Tomcat restarts.
 */
public class InMemoryDemandRequestDao implements DemandRequestDao {

    private static final InMemoryDemandRequestDao INSTANCE = new InMemoryDemandRequestDao(sampleRequests());

    private final Map<String, DemandRequest> requestsByFolio = new ConcurrentHashMap<>();

    public InMemoryDemandRequestDao(List<DemandRequest> requests) {
        requests.forEach(request -> requestsByFolio.put(request.getFolio(), request));
    }

    public static InMemoryDemandRequestDao getInstance() {
        return INSTANCE;
    }

    @Override
    public List<DemandRequest> findAll() {
        return List.copyOf(requestsByFolio.values());
    }

    @Override
    public List<DemandRequest> findByCourt(String courtName) {
        return requestsByFolio.values().stream()
                .filter(request -> request.getCourtName().equals(courtName))
                .toList();
    }

    @Override
    public Optional<DemandRequest> findByFolio(String folio) {
        return folio == null ? Optional.empty() : Optional.ofNullable(requestsByFolio.get(folio.trim()));
    }

    @Override
    public void update(DemandRequest demandRequest) {
        // The object in the map is the one that was changed; a JDBC DAO runs the UPDATE here
        requestsByFolio.put(demandRequest.getFolio(), demandRequest);
    }

    private static List<DemandRequest> sampleRequests() {
        String cuautla = "Juzgado de Cuautla";
        String clerk = "Laura Méndez";
        List<DemandRequest> requests = new ArrayList<>();

        requests.add(new DemandRequest("S-000127/2026", LocalDate.parse("2026-10-26"), cuautla, "12", "Familiar",
                person(PartyRole.PLAINTIFF, "Ana Torres Aguilar", "TOAA900512KL3", "735 112 4598",
                        "ana.torres@ejemplo.mx"),
                person(PartyRole.DEFENDANT, "Luis Torres López", "TOLL880203H45", "735 109 3321", null),
                "Lic. Marcos Rivera Peña", clerk,
                List.of(new Annex("Escrito inicial de demanda.pdf", 1240), new Annex("INE de la demandante.pdf", 380),
                        new Annex("Acta de matrimonio.pdf", 512)),
                null));

        requests.add(new DemandRequest("S-000126/2026", LocalDate.parse("2026-10-26"), cuautla, "12", "Civil",
                Party.unidentified(PartyRole.PLAINTIFF),
                company(PartyRole.DEFENDANT, "Minera del Sur S.A. de C.V.", "MSU010203KL9", "777 310 2200",
                        "juridico@mineradelsur.ejemplo.mx"),
                "Lic. Sofía Campos Ruiz", clerk,
                List.of(new Annex("Escrito inicial de demanda.pdf", 980), new Annex("Contrato de arrendamiento.pdf", 2150)),
                new DuplicityWarning("Juzgado de Yautepec", "Civil", "Continuó con el registro")));

        requests.add(new DemandRequest("S-000125/2026", LocalDate.parse("2026-10-23"), cuautla, "12", "Mercantil",
                company(PartyRole.PLAINTIFF, "Grupo Ávila S.A. de C.V.", "GAV020304QW1", "735 150 7788",
                        "contacto@grupoavila.ejemplo.mx"),
                company(PartyRole.DEFENDANT, "Ferretera del Sur S.A.", "FSU990101AA2", "735 140 0021", null),
                "Lic. Héctor Ávila Núñez", clerk,
                List.of(new Annex("Escrito inicial de demanda.pdf", 1530), new Annex("Pagarés.pdf", 3420)),
                new DuplicityWarning("Juzgado de Cuernavaca", "Mercantil", "Continuó con el registro")));

        DemandRequest penal = new DemandRequest("S-000124/2026", LocalDate.parse("2026-10-23"), cuautla, "12",
                "Penal",
                person(PartyRole.PLAINTIFF, "Juan Ruiz Hernández", "RUHJ850921MT2", "735 188 4410", null),
                Party.unidentified(PartyRole.DEFENDANT), null, clerk,
                List.of(new Annex("Denuncia por escrito.pdf", 860)), null);
        penal.recordAdmissible("2026/00431/12", "Mesa 2", clerk, LocalDateTime.parse("2026-10-24T10:15"));
        requests.add(penal);

        DemandRequest familyRejected = new DemandRequest("S-000123/2026", LocalDate.parse("2026-10-22"), cuautla,
                "12", "Familiar",
                person(PartyRole.PLAINTIFF, "María Gómez Salinas", "GOSM910707AB4", "735 133 9087", null),
                person(PartyRole.DEFENDANT, "Raúl Ortiz Campos", "OICR870115QP8", null, null),
                null, clerk, List.of(new Annex("Escrito inicial de demanda.pdf", 720)), null);
        familyRejected.recordInadmissible("La demanda no trae la firma autógrafa de la promovente.", clerk,
                LocalDateTime.parse("2026-10-22T13:40"));
        requests.add(familyRejected);

        DemandRequest civil = new DemandRequest("S-000122/2026", LocalDate.parse("2026-10-21"), cuautla, "12",
                "Civil",
                person(PartyRole.PLAINTIFF, "Rosa León Martínez", "LEMR800330KD1", "735 121 6600", null),
                person(PartyRole.DEFENDANT, "Hugo Vega Torres", "VETH760812LL0", null, null),
                "Lic. Paola Fuentes Díaz", clerk, List.of(new Annex("Escrito inicial de demanda.pdf", 1100)), null);
        civil.recordAdmissible("2026/00428/12", "Mesa 1", clerk, LocalDateTime.parse("2026-10-21T12:05"));
        requests.add(civil);

        DemandRequest mercantile = new DemandRequest("S-000121/2026", LocalDate.parse("2026-10-20"), cuautla,
                "12", "Mercantil",
                person(PartyRole.PLAINTIFF, "Ana Lara Ortega", "LAOA930214PZ6", "735 177 0034", null),
                company(PartyRole.DEFENDANT, "Banco del Sur S.A.", "BSU950505MN3", null,
                        "litigios@bancodelsur.ejemplo.mx"),
                null, clerk, List.of(new Annex("Escrito inicial de demanda.pdf", 940)), null);
        mercantile.recordAdmissible("2026/00425/12", "Mesa 3", clerk, LocalDateTime.parse("2026-10-20T16:30"));
        requests.add(mercantile);

        DemandRequest familyRejected2 = new DemandRequest("S-000120/2026", LocalDate.parse("2026-10-20"), cuautla,
                "12", "Familiar",
                person(PartyRole.PLAINTIFF, "Carla Díaz Rangel", "DIRC950620TU9", "735 166 2245", null),
                person(PartyRole.DEFENDANT, "Iván Ibarra Soto", "IASI901101RK2", null, null),
                null, clerk, List.of(new Annex("Escrito inicial de demanda.pdf", 650)), null);
        familyRejected2.recordInadmissible("El asunto corresponde a un juzgado de otro distrito judicial.", clerk,
                LocalDateTime.parse("2026-10-20T11:10"));
        requests.add(familyRejected2);

        DemandRequest civil2 = new DemandRequest("S-000119/2026", LocalDate.parse("2026-10-19"), cuautla, "12",
                "Civil",
                person(PartyRole.PLAINTIFF, "Julio Paz Robles", "PARJ820909BC5", "735 199 8812", null),
                person(PartyRole.DEFENDANT, "Inés Ríos Mena", "RIMI840417WX7", null, null),
                null, clerk, List.of(new Annex("Escrito inicial de demanda.pdf", 1010)), null);
        civil2.recordAdmissible("2026/00419/12", "Mesa 4", clerk, LocalDateTime.parse("2026-10-19T09:50"));
        requests.add(civil2);

        // Another court: never listed for Cuautla users, and its detail is denied to them
        requests.add(new DemandRequest("S-000045/2026", LocalDate.parse("2026-10-25"), "Juzgado de Jojutla", "07",
                "Familiar",
                person(PartyRole.PLAINTIFF, "Teresa Ortiz Sánchez", "TOSA850312ML4", "734 101 2030", null),
                person(PartyRole.DEFENDANT, "Pedro Gil Luna", "PEGL800101HD1", null, null),
                null, "Marta Cruz", List.of(new Annex("Escrito inicial de demanda.pdf", 800)), null));
        return requests;
    }

    private static Party person(PartyRole role, String name, String rfc, String phone, String email) {
        return new Party(role, name, "Persona física", rfc, phone, email);
    }

    private static Party company(PartyRole role, String name, String rfc, String phone, String email) {
        return new Party(role, name, "Persona moral", rfc, phone, email);
    }
}
