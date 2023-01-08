package fr.univtln.lhd.model.entities.dao.slots;

import fr.univtln.lhd.exceptions.IdException;
import fr.univtln.lhd.model.entities.dao.DAO;
import fr.univtln.lhd.model.entities.dao.Datasource;
import fr.univtln.lhd.model.entities.dao.users.ProfessorDAO;
import fr.univtln.lhd.model.entities.slots.Group;
import fr.univtln.lhd.model.entities.slots.Slot;
import fr.univtln.lhd.model.entities.users.Professor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.threeten.extra.Interval;

import java.sql.*;
import java.time.Instant;
import java.util.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

@Slf4j
public class SlotDAO implements DAO<Slot> {
    private static final String FORMATTED_SLOT = "id, to_char(lower(timerange) , 'YYYY-MM-DD\"T\"HH24:MI:SS.USTZH:TZM')  as \"begin\", to_char(upper(timerange) , 'YYYY-MM-DD\"T\"HH24:MI:SS.USTZH:TZM') as \"end\",classroom,memo,subject,type";
    private static final String GET_STMT = "SELECT " + FORMATTED_SLOT + " FROM SLOTS WHERE ID=?";//NOSONAR
    private static final String SAVE_STMT = "INSERT INTO SLOTS VALUES (DEFAULT,?::tstzrange,?,?,?,?)";
    private static final String UPDATE_STMT = "UPDATE SLOTS SET TIMERANGE=?::tstzrange, CLASSROOM=?, MEMO=?, SUBJECT=?, TYPE=? WHERE ID=?";
    private static final String GET_SLOT_FROM_PROFESSOR_STMT = "SELECT " + FORMATTED_SLOT + " FROM  slots JOIN professor_slot on id_slot = slots.id WHERE id_professor= (?)";//NOSONAR
    private static final String GET_SLOT_FROM_PROFESSOR_TIME_STMT = "SELECT " + FORMATTED_SLOT + " FROM  slots JOIN professor_slot on id_slot = slots.id WHERE id_professor= (?) and lower(timerange) >= (?) and upper(timerange) < (?)";//NOSONAR

    private static final String GET_SLOT_FROM_GROUP_STMT = "SELECT " + FORMATTED_SLOT + " FROM  slots JOIN group_slot on id_slot = slots.id WHERE id_group= (?)";//NOSONAR
    private static final String GET_SLOT_FROM_GROUP_TIME_STMT = "SELECT " + FORMATTED_SLOT + " FROM  slots JOIN group_slot on id_slot = slots.id WHERE id_group= (?) and lower(timerange) >= (?) and upper(timerange) < (?)";//NOSONAR

    private static final String GETALL_STMT = "SELECT * FROM SLOTS";//NOSONAR
    private static final String DELETE_STMT = "DELETE FROM slots WHERE id=(?)";

    private SlotDAO() {
    }

    public static SlotDAO getInstance() {
        return new SlotDAO();
    }

    /**
     * A wrapper for selection to avoid result set duplication
     *
     * @param rs a ResultSet stemming from a query
     * @return the Slot to select
     * @throws SQLException caller to manage
     * @throws IdException  caller to manage
     */
    private Slot getFromResultSet(final ResultSet rs) throws SQLException, IdException {
        final long id = (rs.getLong("ID"));
        final Slot result = Slot.getInstance(
                Slot.SlotType.valueOf(rs.getString("TYPE")),
                ClassroomDAO.getInstance().get(rs.getLong("CLASSROOM")).orElseThrow(SQLException::new),
                SubjectDAO.getInstance().get(rs.getLong("SUBJECT")).orElseThrow(SQLException::new),
                new ArrayList<>(GroupDAO.getInstance().getGroupOfSlot(id)), //the returned list is unmodifiable, requiring a copy.
                ProfessorDAO.of().getProfessorOfSlots(id),

                Interval.of(Instant.parse(rs.getString("begin")),
                        Instant.parse(rs.getString("end"))),
                rs.getString("MEMO")
        );
        result.setId(id);
        return result;
    }

    /**
     * Get a SQL query from parameters with a date-based filter
     *
     * @param id        the id pf the slot to get
     * @param timerange the date to base the filter upon
     * @param query     SQL query to be executed with passed parameters
     * @return A set of corresponding slot entities
     * @throws SQLException if the query goes wrong
     */
    private Set<Slot> getList(final long id, final Interval timerange, @NonNull final String query) throws SQLException {
        Slot result;
        Set<Slot> slotSet = new HashSet<>();
        try (Connection conn = Datasource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setLong(1, id);
            if (timerange != null) {
                stmt.setTimestamp(2, Timestamp.from(timerange.getStart()));
                stmt.setTimestamp(3, Timestamp.from(timerange.getEnd()));
            }
            stmt.executeQuery();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                result = getFromResultSet(rs);
                slotSet.add(result);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw e;
        } catch (IdException e) {
            log.error(e.getMessage());
        }
        return Collections.unmodifiableSet(slotSet);
    }

    /**
     * Get a SQL query from parameters
     *
     * @param id    the id pf the slot to get
     * @param query SQL query to be executed with passed parameters
     * @return A set of corresponding slot entities
     * @throws SQLException if the query goes wrong
     */
    private Set<Slot> getList(final long id, final @NonNull String query) throws SQLException {
        return getList(id, null, query);
    }

    /**
     * Getter for one Slot
     *
     * @param id numerical long identifier for getting the Slot
     * @return May return one Slot instance
     */
    @Override
    public Optional<Slot> get(final long id) throws SQLException {
        final Set<Slot> slotSet = getList(id, GET_STMT);
        if (slotSet.size() > 1) throw new IllegalStateException("more than one element from get");
        return getList(id, GET_STMT).stream().findFirst();
    }

    /**
     * Getter for all Slots
     *
     * @return List of all Slots
     */
    @Override
    public List<Slot> getAll() throws SQLException {
        List<Slot> slotList = new ArrayList<>();
        try (Connection conn = Datasource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GETALL_STMT);
             ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                slotList.add(get(rs.getLong("ID")).orElseThrow(SQLException::new));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw e;
        }
        return slotList;
    }


    /**
     * Take a group and return a List of the slot with this group
     *
     * @param group the group taken
     * @return List of slot
     * @throws SQLException if an error occurs
     */
    public List<Slot> getSlotOfGroup(Group group) throws SQLException {
        return List.copyOf(getList(group.getId(), GET_SLOT_FROM_GROUP_STMT));
    }

    /**
     * Take a professor and return a List of the slot with this professor
     *
     * @param professor the professor taken
     * @return Set of slot
     * @throws SQLException if an error occurs
     */
    public Set<Slot> getSlotOfAProfessor(Professor professor, Interval timerange) throws SQLException {
        return getList(professor.getId(), timerange, GET_SLOT_FROM_PROFESSOR_TIME_STMT);
    }

    public List<Slot> getSlotOfGroup(Group group, Interval timerange) throws SQLException {
        return List.copyOf(getList(group.getId(), timerange, GET_SLOT_FROM_GROUP_TIME_STMT));
    }

    private String getTimeRangeOfInterval(Interval interval) {
        return "[\"" + interval.getStart().toString() + "\",\"" + interval.getEnd().toString() + "\")";
    }

    /**
     * Save Slot to Database
     *
     * @param slot Slot object to save
     */
    @Override
    public void save(Slot slot) throws SQLException {
        try (Connection conn = Datasource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SAVE_STMT, RETURN_GENERATED_KEYS)
        ) {
            setSlotEntityAttributes(slot, stmt);

            stmt.executeUpdate();
            ResultSet idSet = stmt.getGeneratedKeys();
            idSet.next();
            long newSlotId = idSet.getLong(1);
            slot.setId(newSlotId);

            GroupDAO groupDAO = GroupDAO.getInstance();
            groupDAO.save(newSlotId, slot.getGroup().parallelStream().unordered().mapToLong(Group::getId).toArray());

            ProfessorDAO professorDAO = ProfessorDAO.of();
            professorDAO.save(newSlotId, slot.getProfessors().parallelStream().unordered().mapToLong(Professor::getId).toArray());

        } catch (SQLException e) {
            log.error(e.getMessage());
            throw e;
        } catch (IdException e) {
            log.error(e.getMessage());
        }
    }

    private void setSlotEntityAttributes(Slot slot, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getTimeRangeOfInterval(slot.getTimeRange()));
        stmt.setLong(2, slot.getClassroom().getId());
        stmt.setObject(3, slot.getMemo().orElse(null));
        stmt.setLong(4, slot.getSubject().getId());
        stmt.setString(5, slot.getType().name());
    }

    /**
     * Update Data of Slot Table
     *
     * @param slot Slot instance to update
     */
    @Override
    public Slot update(final Slot slot) throws SQLException {
        try (Connection conn = Datasource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STMT)
        ) {
            setSlotEntityAttributes(slot, stmt);

            stmt.setLong(6, slot.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw e;
        }

        return slot;
    }

    /**
     * Delete Slot instance from Database
     *
     * @param slot Slot object to delete
     */
    @Override
    public void delete(final Slot slot) throws SQLException {
        try (Connection conn = Datasource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_STMT)
        ) {
            stmt.setLong(1, slot.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw e;
        }
    }


}

