package systemtests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_SALARY_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PROJECT_DESC_OASIS;
import static seedu.address.logic.commands.CommandTestUtil.SALARY_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.SALARY_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PROJECT_OASIS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.KEYWORD_MATCHING_MEIER;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import guitests.GuiRobot;
import guitests.guihandles.LoginHandle;
import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.LogoutCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.SelfEditCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.model.Model;
import seedu.address.model.leaveapplication.LeaveApplication;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Password;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Salary;
import seedu.address.model.person.Username;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;
import seedu.address.ui.testutil.EventsCollectorRule;

public class SelfEditCommandSystemTest extends AddressBookSystemTest {

    @Rule
    public final EventsCollectorRule eventsCollectorRule = new EventsCollectorRule();

    private final GuiRobot guiRobot = new GuiRobot();

    private LoginHandle loginHandle;

    /**
     * Logs out then logs in as a normal user.
     */
    public void loginAsUser() {

        //Start off logged out.
        assert getModel().getLoggedInUser() != null;
        //use command box
        executeCommand(LogoutCommand.COMMAND_WORD);
        guiRobot.pauseForHuman();

        loginHandle = getMainWindowHandle().getLoginHandle();
        attemptLoginUser();
    }

    /**
     * Attempts to log in as a normal user. The current address book must show a login screen.
     */
    public void attemptLoginUser() {
        Person p = getModel().getAddressBook().getPersonList().get(0);
        String username = p.getUsername().username;
        String password = p.getPassword().password;
        guiRobot.pauseForHuman();

        loginHandle.attemptLogIn(username, password);
        guiRobot.pauseForHuman();

        assert getModel().getLoggedInUser() != null;
        assert getModel().getLoggedInUser().getPerson().equals(p);

        refreshAllQueries();
    }

    @Test
    public void editWhileAdmin(){
        Model model = getModel();
        assertCommandFailure("myself -p 99900999", SelfEditCommand.ADMIN_EDIT_ERROR);
        //ensure no changes have been made
        assert model.equals(getModel());
    }

    @Test
    public void edit() {
        loginAsUser();

        Model model = getModel();

        /* Case: edit all fields, command with leading spaces, trailing spaces and multiple spaces between each field
         * -> edited
         */
        Index index = INDEX_FIRST_PERSON;
        String command = " " + SelfEditCommand.COMMAND_WORD + "  " + PHONE_DESC_BOB + " " + EMAIL_DESC_BOB
            + "  " + ADDRESS_DESC_BOB + "  " + PROJECT_DESC_OASIS + " ";
        Person editedPerson = new PersonBuilder(model.getFilteredPersonList().get(0))
            .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB).withAddress(VALID_ADDRESS_BOB)
            .withProjects(VALID_PROJECT_OASIS).build();
        assertCommandSuccess(command, index, editedPerson);

        /* Case: undo editing the last person in the list -> last person restored */
        command = UndoCommand.COMMAND_WORD;
        String expectedResultMessage = UndoCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, model, expectedResultMessage);

        /* Case: redo editing the last person in the list -> last person edited again */
        command = RedoCommand.COMMAND_WORD;
        expectedResultMessage = RedoCommand.MESSAGE_SUCCESS;
        model.updatePerson(
            getModel().getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()), editedPerson);
        assertCommandSuccess(command, model, expectedResultMessage);

        /* Case: edit a person with new values same as existing values -> edited */
        command = SelfEditCommand.COMMAND_WORD + " " + PHONE_DESC_BOB + EMAIL_DESC_BOB
            + ADDRESS_DESC_BOB;
        assertCommandSuccess(command, index, editedPerson);

        /* Case: edit a person with new values same as another person's values -> edited */
        assertTrue(getModel().getAddressBook().getPersonList().contains(editedPerson));
        index = INDEX_SECOND_PERSON;
        assertNotEquals(getModel().getFilteredPersonList().get(index.getZeroBased()), editedPerson);
        String otherPhone = model.getFilteredPersonList().get(index.getZeroBased()).getPhone().value;
        String otherAddress = model.getFilteredPersonList().get(index.getZeroBased()).getAddress().value;
        command = SelfEditCommand.COMMAND_WORD + " " + PREFIX_PHONE + " " + otherPhone + " " + PREFIX_ADDRESS + " "
            + otherAddress + " ";
        editedPerson = new PersonBuilder(editedPerson).withPhone(otherPhone).withAddress(otherAddress).build();
        assertCommandSuccess(command, INDEX_FIRST_PERSON, editedPerson);

        /* --------------------- Performing edit operation while a person card is selected -------------------------- */

        /* Case: selects first card in the person list, edit a person -> edited, card selection remains unchanged but
         * browser url changes
         */
        /*
        showAllPersons();
        index = INDEX_FIRST_PERSON;
        originalUsername = model.getFilteredPersonList().get(index.getZeroBased()).getUsername();
        originalPassword = model.getFilteredPersonList().get(index.getZeroBased()).getPassword();
        selectPerson(index);
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
            + ADDRESS_DESC_AMY + SALARY_DESC_AMY + PROJECT_DESC_OASIS;
        // this can be misleading: card selection actually remains unchanged but the
        // browser's url is updated to reflect the new person's name
        Person newPerson = new PersonBuilder(AMY).withUsername(originalUsername.username)
            .withPassword(originalPassword.password).build();
        assertCommandSuccess(command, index, newPerson, index);
        */

        /* --------------------------------- Performing invalid edit operation -------------------------------------- */

        /* Case: missing all fields -> rejected */
        assertCommandFailure(SelfEditCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased(),
            String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, SelfEditCommand.MESSAGE_USAGE));

        /* Case: invalid phone -> rejected */
        assertCommandFailure(SelfEditCommand.COMMAND_WORD + " " + INVALID_PHONE_DESC,
            Phone.MESSAGE_PHONE_CONSTRAINTS);

        /* Case: invalid email -> rejected */
        assertCommandFailure(SelfEditCommand.COMMAND_WORD + " " + INVALID_EMAIL_DESC,
            Email.MESSAGE_EMAIL_CONSTRAINTS);

        /* Case: invalid address -> rejected */
        assertCommandFailure(SelfEditCommand.COMMAND_WORD + " " + INVALID_ADDRESS_DESC,
            Address.MESSAGE_ADDRESS_CONSTRAINTS);
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Index, Person, Index)} except that
     * the browser url and selected card remain unchanged.
     * @param toEdit the index of the current model's filtered list
     * @see EditCommandSystemTest#assertCommandSuccess(String, Index, Person, Index)
     */
    private void assertCommandSuccess(String command, Index toEdit, Person editedPerson) {
        assertCommandSuccess(command, toEdit, editedPerson, null);
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Model, String, Index)} and in addition,<br>
     * 1. Asserts that result display box displays the success message of executing {@code EditCommand}.<br>
     * 2. Asserts that the model related components are updated to reflect the person at index {@code toEdit} being
     * updated to values specified {@code editedPerson}.<br>
     * @param toEdit the index of the current model's filtered list.
     * @see EditCommandSystemTest#assertCommandSuccess(String, Model, String, Index)
     */
    private void assertCommandSuccess(String command, Index toEdit, Person editedPerson,
                                      Index expectedSelectedCardIndex) {
        Model expectedModel = getModel();
        expectedModel.updatePerson(expectedModel.getFilteredPersonList().get(toEdit.getZeroBased()), editedPerson);
        expectedModel.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        assertCommandSuccess(command, expectedModel,
            String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, editedPerson), expectedSelectedCardIndex);
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Model, String, Index)} except that the
     * browser url and selected card remain unchanged.
     * @see EditCommandSystemTest#assertCommandSuccess(String, Model, String, Index)
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage) {
        assertCommandSuccess(command, expectedModel, expectedResultMessage, null);
    }

    /**
     * Executes {@code command} and in addition,<br>
     * 1. Asserts that the command box displays an empty string.<br>
     * 2. Asserts that the result display box displays {@code expectedResultMessage}.<br>
     * 3. Asserts that the browser url and selected card update accordingly depending on the card at
     * {@code expectedSelectedCardIndex}.<br>
     * 4. Asserts that the status bar's sync status changes.<br>
     * 5. Asserts that the command box has the default style class.<br>
     * Verifications 1 and 2 are performed by
     * {@code AddressBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see AddressBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     * @see AddressBookSystemTest#assertSelectedCardChanged(Index)
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage,
                                      Index expectedSelectedCardIndex) {
        executeCommand(command);
        expectedModel.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);
        assertCommandBoxShowsDefaultStyle();
        if (expectedSelectedCardIndex != null) {
            assertSelectedCardChanged(expectedSelectedCardIndex);
        } else {
            assertSelectedCardUnchanged();
        }
        assertStatusBarUnchangedExceptSyncStatus();
    }

    /**
     * Executes {@code command} and in addition,<br>
     * 1. Asserts that the command box displays {@code command}.<br>
     * 2. Asserts that result display box displays {@code expectedResultMessage}.<br>
     * 3. Asserts that the browser url, selected card and status bar remain unchanged.<br>
     * 4. Asserts that the command box has the error style.<br>
     * Verifications 1 and 2 are performed by
     * {@code AddressBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see AddressBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandFailure(String command, String expectedResultMessage) {
        Model expectedModel = getModel();

        executeCommand(command);
        assertApplicationDisplaysExpected(command, expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsErrorStyle();
        assertStatusBarUnchanged();
    }
}
