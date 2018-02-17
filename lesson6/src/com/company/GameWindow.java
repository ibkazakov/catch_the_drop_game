package com.company;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashSet;


public class GameWindow extends JFrame {

    private static GameWindow gameWindow;
    private static long last_frame_time;
    private static Image game_over;
    private static Image drop;
    private static double drop_v = 150;
    private static double clinamen_top = 0.5;
    private static double clinamen_left = 3.0;
    public static int dropsClicked = 0;
    private static int score = 0;
    private static int level = 1;
    private static boolean gameOver = false;
    private static DropSet drops;
    private static GameField game_field = new GameField();
    private static JButton replayButton;
    private static Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();

    private static void refreshTitle() {
		gameWindow.setTitle("Score: " + score + " Level: " + level);
	}


    public static void main(String[] args) throws IOException {
		game_over = ImageIO.read(GameWindow.class.getResourceAsStream("game_over.png"));
		drop = ImageIO.read(GameWindow.class.getResourceAsStream("drop.png"));
	    gameWindow = new GameWindow();
	    gameWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    gameWindow.setLocation(0, 0);
		gameWindow.setSize(sSize);
	    gameWindow.setResizable(false);
	    refreshTitle();
		last_frame_time = System.nanoTime();

	    game_field.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				drops.click(x, y);
			}
		});

	    game_field.setBackground(new Color(0xAF, 0xEE, 0xEE));
	    gameWindow.add(game_field);
	    drops = new DropSet();

	    replayButton = new JButton("Replay");
	    replayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gameOver = false;
				dropsClicked = 0;
				score = 0;
				level = 1;
				drops.createDrop();
				refreshTitle();
				replayButton.setVisible(false);
			}
		});
		replayButton.setBounds((int)sSize.getWidth() - 110, 10, 90, 35);
		replayButton.setBackground(Color.RED);
		replayButton.setForeground(Color.WHITE);
	    replayButton.setVisible(false);
		game_field.setLayout(null);
	    game_field.add(replayButton);

	    gameWindow.setVisible(true);
    }

    private static void onRepaint(Graphics g) {
    	long current_time = System.nanoTime();
    	double delta_time = (current_time - last_frame_time) * 0.000000001f;
    	last_frame_time = current_time;

		if (!gameOver) drops.paint(g, delta_time);
		if (gameOver) g.drawImage(game_over, (sSize.width - game_over.getWidth(null)) / 2,
				(sSize.height - game_over.getWidth(null)) / 2, null);
	}

	private static class GameField extends JPanel {
    	@Override
		protected void paintComponent(Graphics g) {
    		super.paintComponent(g);
    		onRepaint(g);
    		repaint();
		}
	}

	private static class Drop{

    	public Drop() {
			update();
		}

		public void clinUpdate() {
			clinLeft = drop_v * clinamen_left * (Math.random() - 0.5);
			clinTop = drop_v * clinamen_top * (Math.random() - 0.5);
			clinDelay = (Math.random() + 0.5) * 0.5;
			clinDelayStore = 0.0;
		}

		public void update() {
    		top = -100.00;
			left = Math.random() * (game_field.getWidth() - drop.getWidth(null));
			timeDelayStore = 0.0;
			clinUpdate();
		}

    	private double left;
    	private double top;

    	private double clinLeft;
    	private double clinTop;

    	private static final double DELAY = 1;
    	private double clinDelay;
    	private double clinDelayStore = 0.0;
    	private double timeDelayStore = 0.0;

    	public boolean isDestroyed() {
    		return ((int)top > gameWindow.getHeight());
		}

   		public boolean isClick(int x, int y) {
			double right = left + drop.getWidth(null);
			double bottom = top + drop.getHeight(null);
			boolean isClick = ((int)x) >= left && ((int)x) <= right && ((int)y) >= top && ((int)y) <= bottom;
			return isClick;
		}

		public void paint(Graphics g, double delta_time) {
			g.drawImage(drop, (int)left, (int)top, null);
			move(delta_time);
		}

		private void move(double delta_time) {
			if (timeDelayStore >= DELAY*(level - 1)) {
				int width = game_field.getWidth() - drop.getWidth(null);
				top = top + (drop_v + clinTop) * delta_time;
				left = (left + (clinLeft) * delta_time + width) % width;
				if (clinDelayStore >= clinDelay) {
					clinUpdate();
				}
				else {
					clinDelayStore += delta_time;
				}
			}
			else {
				timeDelayStore += delta_time;
			}
		}

		public void click() {
			update();
			dropsClicked++;
			score = score + level;
			if (dropsClicked % 50 == 0) {
				level++;
				drops.createDrop();
			}
			refreshTitle();
		}
	}

	private static class DropSet extends HashSet<Drop> {
    	public DropSet() {
    		createDrop();
		}

    	public void createDrop() {
    		this.add(new Drop());
		}

		public void paint(Graphics g, double delta_time) {
    		for (Drop drop : this) {
    			drop.paint(g, delta_time);
    			if (drop.isDestroyed()) {
					gameOver = true;
					clear();
					replayButton.setVisible(true);
					break;
				}
			}
		}
		public void click(int x, int y) {
    		for (Drop drop : this) {
    			if (drop.isClick(x, y)) {
    				drop.click();
    				break;
				}
			}
		}
	}

}
