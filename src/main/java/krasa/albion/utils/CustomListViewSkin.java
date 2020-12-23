package krasa.albion.utils;


import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;

public class CustomListViewSkin<T> extends ListViewSkin<T> {

	private static final double DISTANCE = 10;
	private static final double PERCENTAGE = 0.05;

	private AnimationTimer scrollAnimation = new AnimationTimer() {

		@Override
		public void handle(long now) {
			if (direction == -1) {
				getVirtualFlow().scrollPixels(-DISTANCE);
			} else if (direction == 1) {
				getVirtualFlow().scrollPixels(DISTANCE);
			}
		}

	};

	private Rectangle2D leftUpArea;
	private Rectangle2D rightDownArea;

	private int direction = 0;
	private int anchorIndex = -1;

	public CustomListViewSkin(final ListView<T> control) {
		super(control);
		final var flow = getVirtualFlow();
		final var factory = flow.getCellFactory();

		// decorate the actual cell factory
		flow.setCellFactory(vf -> {
			final var cell = factory.call(flow);

			// handle drag start
			cell.addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
				if (control.getSelectionModel().getSelectionMode() == SelectionMode.MULTIPLE) {
					event.consume();
					cell.startFullDrag();
					anchorIndex = cell.getIndex();
				}
			});

			// handle selecting items when the mouse-drag enters the cell
			cell.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
				event.consume();
				if (event.getGestureSource() != cell) {
					final var model = control.getSelectionModel();
					if (anchorIndex < cell.getIndex()) {
						model.selectRange(anchorIndex, cell.getIndex() + 1);
					} else {
						model.selectRange(cell.getIndex(), anchorIndex + 1);
					}
				}
			});

			return cell;
		});

		// handle the auto-scroll functionality
		flow.addEventHandler(MouseDragEvent.MOUSE_DRAG_OVER, event -> {
			event.consume();
			if (leftUpArea.contains(event.getX(), event.getY())) {
				direction = -1;
				scrollAnimation.start();
			} else if (rightDownArea.contains(event.getX(), event.getY())) {
				direction = 1;
				scrollAnimation.start();
			} else {
				direction = 0;
				scrollAnimation.stop();
			}
		});

		// stop the animation when the mouse exits the flow/list (desired?)
		flow.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> {
			event.consume();
			scrollAnimation.stop();
		});

		// handle stopping the animation and reset the state when the mouse
		// is released. Added to VirtualFlow because it doesn't matter
		// which cell receives the event.
		flow.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
			if (anchorIndex != -1) {
				event.consume();
				anchorIndex = -1;
				scrollAnimation.stop();
			}
		});

		updateAutoScrollAreas();
		registerChangeListener(control.orientationProperty(), obs -> updateAutoScrollAreas());
		registerChangeListener(flow.widthProperty(), obs -> updateAutoScrollAreas());
		registerChangeListener(flow.heightProperty(), obs -> updateAutoScrollAreas());
	}

	// computes the regions where the mouse needs to be
	// in order to start auto-scrolling. The regions depend
	// on the orientation of the ListView.
	private void updateAutoScrollAreas() {
		final var flow = getVirtualFlow();
		switch (getSkinnable().getOrientation()) {
			case HORIZONTAL:
				final double width = flow.getWidth() * PERCENTAGE;
				leftUpArea = new Rectangle2D(0, 0, width, flow.getHeight());
				rightDownArea = new Rectangle2D(flow.getWidth() - width, 0, width, flow.getHeight());
				break;
			case VERTICAL:
				final double height = flow.getHeight() * PERCENTAGE;
				leftUpArea = new Rectangle2D(0, 0, flow.getWidth(), height);
				rightDownArea = new Rectangle2D(0, flow.getHeight() - height, flow.getWidth(), height);
				break;
			default:
				throw new AssertionError();
		}
	}

	@Override
	public void dispose() {
		unregisterChangeListeners(getSkinnable().orientationProperty());
		unregisterChangeListeners(getVirtualFlow().widthProperty());
		unregisterChangeListeners(getVirtualFlow().heightProperty());
		super.dispose();

		scrollAnimation.stop();
		scrollAnimation = null;
	}
}