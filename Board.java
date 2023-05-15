package mines;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class Board extends JPanel {
	private static final long serialVersionUID = 6195235521361212179L;
	
	private static final int NUM_IMAGES = 13;
    private static final int CELL_SIZE = 15;

    private static final int COVER_FOR_CELL = 10;
    private static final int MARK_FOR_CELL = 10;
    private static final int EMPTY_CELL = 0;
    private static final int MINE_CELL = 9;
    private static final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL;
    private static final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;

    private static final int DRAW_MINE = 9;
    private static final int DRAW_COVER = 10;
    private static final int DRAW_MARK = 11;
    private static final int DRAW_WRONG_MARK = 12;

    private int[] field;
    private boolean inGame;
    private int minesLeft;
    private  transient Image[] img; 
    private int mines = 40;
    private int rows = 16;
    private int cols = 16;
    private int allCells;
    private JLabel statusbar;

    
    public Board(JLabel statusbar) {

        this.statusbar = statusbar;

        img = new Image[NUM_IMAGES];

        for (int i = 0; i < NUM_IMAGES; i++) {
			img[i] =
                    (new ImageIcon(getClass().getClassLoader().getResource((i)
            			    + ".gif"))).getImage();
        }

        setDoubleBuffered(true);

        addMouseListener(new MinesAdapter());
        newGame();
    }


    public void newGame() {
        Random random = new Random();
        int currentCol, position, cell;
        int i, coveredMinesCount;
        
        // Réinitialiser l'état du jeu
        inGame = true;
        minesLeft = mines;
        statusbar.setText(Integer.toString(minesLeft));
        
        // Initialiser le champ avec toutes les cellules couvertes
        allCells = rows * cols;
        field = new int[allCells];
        Arrays.fill(field, COVER_FOR_CELL);
        
        // Placer des mines au hasard
        i = 0;
        coveredMinesCount = 0;
        while (i < mines) {
            position = random.nextInt(allCells);
            if (field[position] != COVERED_MINE_CELL) {
                field[position] = COVERED_MINE_CELL;
                coveredMinesCount++;
                
                // Mettre à jour le nombre de cellules adjacentes
                currentCol = position % cols;
                if (currentCol > 0) {
                    cell = position - 1 - cols;
                    if (cell >= 0 && field[cell] != COVERED_MINE_CELL) {
                        field[cell]++;
                    }
                    cell = position - 1;
                    if (cell >= 0 && field[cell] != COVERED_MINE_CELL) {
                        field[cell]++;
                    }
                    cell = position + cols - 1;
                    if (cell < allCells && field[cell] != COVERED_MINE_CELL) {
                        field[cell]++;
                    }
                }
                cell = position - cols;
                if (cell >= 0 && field[cell] != COVERED_MINE_CELL) {
                    field[cell]++;
                }
                cell = position + cols;
                if (cell < allCells && field[cell] != COVERED_MINE_CELL) {
                    field[cell]++;
                }
                if (currentCol < cols - 1) {
                    cell = position - cols + 1;
                    if (cell >= 0 && field[cell] != COVERED_MINE_CELL) {
                        field[cell]++;
                    }
                    cell = position + cols + 1;
                    if (cell < allCells && field[cell] != COVERED_MINE_CELL) {
                        field[cell]++;
                    }
                    cell = position + 1;
                    if (cell < allCells && field[cell] != COVERED_MINE_CELL) {
                        field[cell]++;
                    }
                }
                i++;
            }
        }
    }


    private void find_empty_cells(int position) {
        int currentCol = position % cols;
        int cell;

        // Vérifier les cellules à gauche
        if (currentCol > 0) { 
            cell = position - cols - 1;
            if (cell >= 0) checkEmptyCell(cell);

            cell = position - 1;
            if (cell >= 0) checkEmptyCell(cell);

            cell = position + cols - 1;
            if (cell < allCells) checkEmptyCell(cell);
        }

        // Vérifiez les cellules au-dessus et au-dessous
        cell = position - cols;
        if (cell >= 0) checkEmptyCell(cell);

        cell = position + cols;
        if (cell < allCells) checkEmptyCell(cell);

        // Vérifier les cellules à droite
        if (currentCol < (cols - 1)) {
            cell = position - cols + 1;
            if (cell >= 0) checkEmptyCell(cell);

            cell = position + cols + 1;
            if (cell < allCells) checkEmptyCell(cell);

            cell = position + 1;
            if (cell < allCells) checkEmptyCell(cell);
        }
    }

    private void checkEmptyCell(int cell) {
        if (field[cell] > MINE_CELL) {
            field[cell] -= COVER_FOR_CELL;
            if (field[cell] == EMPTY_CELL) {
                find_empty_cells(cell);
            }
        }
    }

    @Override
    public void paint(Graphics g) {

        int cell = 0;
        int uncover = 0;


     // Boucle sur toutes les lignes et colonnes
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                
                // Obtenir la valeur de la cellule pour la ligne et la colonne actuelles
                cell = field[(i * cols) + j];
                
                // Vérifiez si le jeu est terminé et que la cellule actuelle est une mine
                if (inGame && cell == MINE_CELL)
                    inGame = false;
                
                // Vérifiez si le jeu est terminé
                if (!inGame) {
                    // Définir la cellule sur l'image correspondante
                    if (cell == COVERED_MINE_CELL) {
                        cell = DRAW_MINE;
                    } else if (cell == MARKED_MINE_CELL) {
                        cell = DRAW_MARK;
                    } else if (cell > COVERED_MINE_CELL) {
                        cell = DRAW_WRONG_MARK;
                    } else if (cell > MINE_CELL) {
                        cell = DRAW_COVER;
                    }
                } else {
                    // Si le jeu n'est pas terminé
                    if (cell > COVERED_MINE_CELL)
                        cell = DRAW_MARK;
                    else if (cell > MINE_CELL) {
                        cell = DRAW_COVER;
                        uncover++;
                    }
                }
                
                // Dessinez l'image correspondante pour la cellule
                g.drawImage(img[cell], (j * CELL_SIZE), (i * CELL_SIZE), this);
            }
        }

        // Vérifiez si le jeu est gagné ou perdu
        if (uncover == 0 && inGame) {
            inGame = false;
            statusbar.setText("Game won");
        } else if (!inGame)
            statusbar.setText("Game lost");

    }


    class MinesAdapter extends MouseAdapter {
    	// Remplacer la méthode pour gérer l'événement d'appui sur la souris
    	@Override
    	public void mousePressed(MouseEvent e) {

    	    // Obtenir la position X et Y du clic de souris
    	    int x = e.getX();
    	    int y = e.getY();

    	    //Calculer la colonne et la ligne de la cellule cliquée
    	    int cCol = x / CELL_SIZE;
    	    int cRow = y / CELL_SIZE;

    	    // Définir un indicateur pour indiquer si un repaint est nécessaire
    	    boolean rep = false;

    	    // Vérifiez si le jeu n'est pas encore démarré
    	    if (!inGame) {
    	        newGame();
    	        repaint();
    	    }

    	    // Vérifiez si le clic était dans le plateau de jeu
    	    if ((x < cols * CELL_SIZE) && (y < rows * CELL_SIZE)) {

    	        // Vérifiez si le bouton droit de la souris a été cliqué
    	        if (e.getButton() == MouseEvent.BUTTON3) {

    	            // Vérifiez si la cellule contient une mine ou une marque
    	            if (field[(cRow * cols) + cCol] > MINE_CELL) {
    	                rep = true;

    	                // Vérifiez si la cellule est couverte et s'il reste des marques
    	                if (field[(cRow * cols) + cCol] <= COVERED_MINE_CELL) {
    	                    if (minesLeft > 0) {
    	                        field[(cRow * cols) + cCol] += MARK_FOR_CELL;
    	                        minesLeft--;
    	                        statusbar.setText(Integer.toString(minesLeft));
    	                    } else {
    	                        statusbar.setText("No marks left");
    	                    }
    	                } else {
    	                    // Décochez la cellule et incrémentez les marques restantes
    	                    field[(cRow * cols) + cCol] -= MARK_FOR_CELL;
    	                    minesLeft++;
    	                    statusbar.setText(Integer.toString(minesLeft));
    	                }
    	            }

    	        } else {
    	            // Vérifiez si la cellule n'est pas couverte
    	            if (field[(cRow * cols) + cCol] > COVERED_MINE_CELL) {
    	                return;
    	            }

    	            // Vérifiez si la cellule contient une mine ou un numéro
    	            if ((field[(cRow * cols) + cCol] > MINE_CELL) &&
    	                (field[(cRow * cols) + cCol] < MARKED_MINE_CELL)) {

    	                // Découvrez la cellule et définissez le drapeau de repeinture
    	                field[(cRow * cols) + cCol] -= COVER_FOR_CELL;
    	                rep = true;

    	                // Vérifiez si la cellule contient une mine ou est vide
    	                if (field[(cRow * cols) + cCol] == MINE_CELL) {
    	                    inGame = false;
    	                }
    	                if (field[(cRow * cols) + cCol] == EMPTY_CELL) {
    	                    find_empty_cells((cRow * cols) + cCol);
    	                }
    	            }
    	        }

    	        // Repeindre le plateau de jeu si besoin
    	        if (rep) {
    	            repaint();
    	        }
    	    }
    	}
 
}}