import com.controller.ControllerTerminal;
import com.session.Session;
import com.utils.ActionCode;

public class Main {

	private static String capFilePath = "C:\\Program Files (x86)\\Oracle\\Java Card Development Kit 3.0.5u2\\samples\\classic_applets\\Wallet\\applet\\apdu_scripts\\cap-com.sun.jcclassic.samples.wallet.script";

	private static String plainFilePath = "E:\\Lucru\\MyTerminal\\pin";

	private static String pinPlainText;

	private static String pinWrong  = "12346";

	private static ControllerTerminal terminal;

	public static void main(String[] args) throws Exception {

		Session.establishConnection();

		Session.powerUp();

		Session.processCapFile(capFilePath);

		Session.setupEnvironment();

		terminal = new ControllerTerminal();
		terminal.processAction(ActionCode.GET_RULES);

		pinPlainText = Session.loadPinFromFileHexa(plainFilePath);
				
		terminal.processAction(ActionCode.GET_BALANCE);

		terminal.processTransaction(60, ActionCode.CREDIT);
		terminal.processAction(ActionCode.GET_BALANCE);
		terminal.processTransaction(20, ActionCode.CREDIT);
		terminal.processAction(ActionCode.GET_BALANCE);
		terminal.processTransaction(40, ActionCode.CREDIT);
		terminal.processAction(ActionCode.GET_BALANCE);

		terminal.processTransaction(40, ActionCode.DEBIT, pinPlainText);
		terminal.processAction(ActionCode.GET_BALANCE);
		terminal.processTransaction(60, ActionCode.DEBIT, pinPlainText);
		terminal.processAction(ActionCode.GET_BALANCE);
				
		terminal.processTransaction(60, ActionCode.CREDIT, pinPlainText);
		terminal.processAction(ActionCode.GET_BALANCE);
		terminal.processTransaction(101, ActionCode.DEBIT, pinWrong);
		terminal.processAction(ActionCode.GET_BALANCE);
		terminal.processTransaction(120, ActionCode.DEBIT, pinPlainText);
		terminal.processAction(ActionCode.GET_BALANCE);

		Session.powerDown();

	}

}
