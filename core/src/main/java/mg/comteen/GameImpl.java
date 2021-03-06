/**
 * 
 */
package mg.comteen;

import mg.comteen.common.Move;
import mg.comteen.common.Parameter;
import mg.comteen.common.Player;
import mg.comteen.common.Position;
import mg.comteen.common.Result;
import mg.comteen.common.Stone;
import mg.comteen.exception.FanoronaException;
import mg.comteen.rule.Rules;
import mg.comteen.rule.RulesImpl;

/**
 * This is the game entrypoint
 * 
 * @author rama
 */
public class GameImpl extends Move implements Game {

	private int[][] board = new int[5][9];

	private Position currentPosition, nextPosition;

	private Player[] player = new Player[2];
	
	private int lastPlayer = 0;

	private Rules rules = RulesImpl.getInstance();

	public GameImpl() {
		// Initialization player ids respectively 1 and 2
		player[0] = new Player(1);
		player[1] = new Player(2);
	}

	/**
	 * Handle request for processing
	 */
	public Result<String> handleGame(String states, Parameter param) {
		Result<String> res = new Result<String>();
		// Validate parameters
		if (states != null && !states.isEmpty()) {
			// Refresh model board
			setBoard(states);
			//Get arrays position form source state position
			transformIndexTo2DPosition(param.getSourceStatePosition(), 1);

			// The api used or not direction parameter for the processing
			nextMove(param);

			// Begin processing
			param.setCurrentPlayer(getCurrentPlayer());
			param.setCurrentPosition(currentPosition);
			param.setNextPosition(nextPosition);
			param.setDirection();
			
			// Processing
			try {
				rules.processChange(board, param);
				res.setResult(true);
			} catch (Exception ex) {
				res.setMessage(ex.getMessage());
			}
			res.setData(getStringBoard());
		} else {
			res.setMessage("States null or empty");
		}
		return res;
	}

	/**
	 * B = black pion W = white pion
	 * 
	 * @param states
	 */
	public void setBoard(String states) {
		int row = 0;
		int column = 0;
		if (states != null && !states.isEmpty()) {
			for (int i = 0; i < states.length(); i++) {
				char c = states.charAt(i);
				int value = 0;
				switch (c) {
				case Stone.BLACK:
					value = player[0].getId();
					break;
				case Stone.WHITE:
					value = player[1].getId();
					break;
				case Stone.NONE:
					value = 0;
					break;
				default:
				}
				board[row][column] = value;
				if ((++column % 9) == 0) {
					column = 0;
					row++;
				}
			}
		}
	}

	/**
	 * Convert board matrix model to string message
	 * 
	 * @return
	 */
	public String getStringBoard() {
		StringBuilder str = new StringBuilder("");
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 9; j++) {
				if (board[i][j] == 0) {
					str.append("E");
				} else if (board[i][j] == player[0].getId()) {
					str.append("B");
				} else if (board[i][j] == player[1].getId()) {
					str.append("W");
				}
			}
		}
		return str.toString();
	}

	/**
	 * This method allows to find xPoint and yPoint
	 * 
	 * @param index
	 */
	public void transformIndexTo2DPosition(int index, int type) {
		if (type == 1) {
			currentPosition = new Position();
			currentPosition.setY((index % 9) - 1);
			currentPosition.setX((index / 9));
		} else {
			nextPosition = new Position();
			nextPosition.setY((index % 9) - 1);
			nextPosition.setX((index / 9));
		}

	}

	/**
	 * Set the new position by using the direction Get next Position of the
	 * piece if direction -1, we don't use the direction
	 * 
	 * @param direction
	 */
	public void nextMove(Parameter param) {
		int direction = param.getDirection();
		int indexDestination = param.getDestStatePosition();
		if (direction != -1) {
			if (currentPosition != null) {
				nextPosition = getNext(direction, currentPosition);
			} else {
				throw new FanoronaException("Invalid move. Current Position undefined");
			}
		} else {
			transformIndexTo2DPosition(indexDestination, 2);
		}
	}
	
	/**
	 * Get current player based on current position 1/2
	 * @return
	 */
	private Player getCurrentPlayer() {
		Player p = null;
		if (currentPosition != null) {
			int index = board[currentPosition.getX()][currentPosition.getY()] - 1;
			// if it's not the same user
			if(index != lastPlayer) {
				// Reset state last player
				player[lastPlayer].resetStates();
				lastPlayer = index;
			}
			p = player[index];
		}
		return p;
	}

	public int[][] getBoard() {
		return board;
	}

	public void setBoard(int[][] board) {
		this.board = board;
	}

	public Position getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Position currentPosition) {
		this.currentPosition = currentPosition;
	}

	public Position getNextPosition() {
		return nextPosition;
	}

	public void setNextPosition(Position nextPosition) {
		this.nextPosition = nextPosition;
	}

}
