package de.htwk.autolat.BBautOLAT;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.id.Identity;
import de.htwk.autolat.tools.Scoring.ScoreObject;

/**
 * The Class TopListTableModel links a list of ScoreObjects
 * with a TableDataModel interface. The TopListTableModell is used
 * by the CMCTopListController. The ScoreObjectMangager will produce
 * such a list of ScoreObjects for a given course ID.
 */
public class TopListTableModel implements TableDataModel
{		
	/** The scores. */
	private List<ScoreObject> scores;
	
	/** The viewer. */
	private Identity viewer;
	
	/** Date formatter to build a time stamp for sorting purposes */
	private final SimpleDateFormat timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

	/**
	 * Instantiates a new top list table model. The list of scores typically
	 * is created by the ScoreObjectManager. Each table row contains the scores
	 * of a student (i.e. an identity) but only the row with a given identity
	 * (the user that views the table) will be labeled due to privacy protection.
	 *
	 * @param scores a list of ScoreObjects
	 * @param viewer the user that views the table 
	 */
	public TopListTableModel(List<ScoreObject> scores, Identity viewer)
	{ 
    this.scores = scores;
    this.viewer = viewer;
  }
	
	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 3;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getRowCount()
	 */
	public int getRowCount() {
		// TODO Auto-generated method stub
		return scores.size();
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col)
	{
		ScoreObject score = scores.get(row);
		String color = score.getIdentity().equals(viewer) ? " style=\"color: #3C763D; font-weight: bold;\"" : "";
				
		switch(col) {				
						
			//case 0: val = score.getIdentity().equals(viewer) ? score.getIdentity().getName() : "";
			case 0:
				return "<font data-score=\"" + score.getScorePoints() + "\"" + color + ">"
					+ score.getScorePoints()
					+ "</font>";
			case 1:
				return "<font data-points=\"" + score.getScoreSize() + "\"" + color + ">"
					+ score.getScoreSize()
					+ "</font>";
			case 2:	
				return "<font data-date=\"" + timeStamp.format(score.getScoreDate()) + "\"" + color + ">"
					+ DateFormat.getInstance().format(score.getScoreDate())
					+ "</font>";
			default: return null;			
		}
						 		
	}
	
	@Override
	public Object getObject(int row) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setObjects(List objects) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object createCopyWithEmptyList() {
		// TODO Auto-generated method stub
		return null;
	}

}
