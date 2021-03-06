package net.azib.ipscan.gui;

import net.azib.ipscan.config.CommentsConfig;
import net.azib.ipscan.config.GUIConfig;
import net.azib.ipscan.config.Labels;
import net.azib.ipscan.core.ScanningResult;
import net.azib.ipscan.core.ScanningResultList;
import net.azib.ipscan.fetchers.CommentFetcher;
import net.azib.ipscan.gui.util.LayoutHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * The "Show IP Details" Window
 *
 * @author Anton Keks
 */
public class DetailsWindow extends AbstractModalDialog {
	private GUIConfig guiConfig;
	private CommentsConfig commentsConfig;
	private ResultTable resultTable;
	private ScanningResultList scanningResults;

	int resultIndex;
	private Text commentsText;

	public DetailsWindow(GUIConfig guiConfig, CommentsConfig commentsConfig, ResultTable resultTable, ScanningResultList scanningResults) {
		this.guiConfig = guiConfig;
		this.commentsConfig = commentsConfig;
		this.resultTable = resultTable;
		this.scanningResults = scanningResults;
	}
	
	@Override
	protected int getShellStyle() {
		return SWT.TOOL | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE;
	}
	
	@Override
	protected void populateShell() {
		shell.setText(Labels.getLabel("title.details"));
		shell.setLayout(LayoutHelper.formLayout(3, 3, 3));
		shell.setSize(guiConfig.getDetailsWindowSize());

		resultIndex = resultTable.getSelectionIndex();
		ScanningResult result = resultTable.getSelectedResult();
		
		commentsText = new Text(shell, SWT.SEARCH);
		commentsText.pack();
		commentsText.setLayoutData(LayoutHelper.formData(new FormAttachment(0), new FormAttachment(100), null, new FormAttachment(100)));
		CommentsTextListener commentsTextListener = new CommentsTextListener();
		commentsText.addFocusListener(commentsTextListener);
		commentsText.addModifyListener(commentsTextListener);

		String comment = commentsConfig.getComment(result);
		if (comment != null) commentsText.setText(comment);
		else commentsTextListener.focusLost(null);

		Text detailsText = new Text(shell, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		detailsText.setText(result.toString());
		detailsText.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		detailsText.setTabs(32);
		detailsText.setLayoutData(LayoutHelper.formData(new FormAttachment(0), new FormAttachment(100), new FormAttachment(0), new FormAttachment(commentsText)));
						
		Listener traverseListener = new TraverseListener();
		detailsText.addListener(SWT.Traverse, traverseListener);
		commentsText.addListener(SWT.Traverse, traverseListener);
		
		shell.layout();
		detailsText.forceFocus();

		shell.addListener(SWT.Close, event -> guiConfig.setDetailsWindowSize(shell.getSize()));
	}
	
	class CommentsTextListener implements FocusListener, ModifyListener {
		String defaultText = Labels.getLabel("text.comment.edit");
		
		public void focusGained(FocusEvent e) {
			if (commentsText.getText().equals(defaultText)) {
				commentsText.setText("");
				commentsText.setForeground(commentsText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
			}
		}

		public void focusLost(FocusEvent e) {
			if (commentsText.getText().length() == 0) {
				commentsText.setText(defaultText);
				commentsText.setForeground(commentsText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			}
		}

		public void modifyText(ModifyEvent e) {
			String newComment = commentsText.getText();
			if (!defaultText.equals(newComment)) {
				// store the new comment
				commentsConfig.setComment(scanningResults.getResult(resultIndex), newComment);
				// now update the result table for user to immediately see the change
				resultTable.updateResult(resultIndex, CommentFetcher.ID, newComment);
			}
		}
	}
	
	class TraverseListener implements Listener {
		public void handleEvent(Event e) {
			if (e.detail == SWT.TRAVERSE_RETURN) close();
		}
	}
}
