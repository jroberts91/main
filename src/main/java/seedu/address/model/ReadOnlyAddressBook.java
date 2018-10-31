package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.leaveapplication.LeaveApplicationWithEmployee;
import seedu.address.model.person.Person;
import seedu.address.model.project.Assignment;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    /**
     * Returns an unmodifiable view of the leave application list.
     */
    ObservableList<LeaveApplicationWithEmployee> getLeaveApplicationList();

}
