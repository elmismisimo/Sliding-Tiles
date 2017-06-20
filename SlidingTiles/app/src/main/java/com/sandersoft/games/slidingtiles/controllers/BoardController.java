package com.sandersoft.games.slidingtiles.controllers;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.sandersoft.games.slidingtiles.R;
import com.sandersoft.games.slidingtiles.SoundManager;
import com.sandersoft.games.slidingtiles.models.Cell;
import com.sandersoft.games.slidingtiles.utils.Globals;
import com.sandersoft.games.slidingtiles.views.ActivityMain;
import com.sandersoft.games.slidingtiles.views.FragmentGame;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by meixi on 27/05/2017.
 */

public class BoardController implements Parcelable {

    FragmentGame view;
    ArrayList<Cell> cells = new ArrayList<>();
    int size = 4;
    int moves = 0;
    boolean gameOver = false;

    public BoardController(FragmentGame fragmentGame, int size){
        setView(fragmentGame);
        this.size = size;
        initiateBoard();
    }
    public void setView(FragmentGame fragmentGame){
        this.view = fragmentGame;
    }

    public void initiateBoard(){
        gameOver = false;
        moves = 0;
        createBoardTiles();
        /*cells.clear();
        ArrayList<Integer> list = new ArrayList<>();
        for(int i = 0; i < size * size; i++){
            list.add(i);
        }
        //add numbers ins random position
        Random rand = new Random();
        while (list.size() > 0){
            int p = rand.nextInt(list.size());
            cells.add(new Cell(list.get(p)));
            list.remove(p);
        }*/
    }
    public void restart(){
        initiateBoard();
        view.saveGame("");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getActivity());
    }

    public void createBoardTiles(){
        //clear all the cells
        cells.clear();
        //add 0 cell
        cells.add(new Cell(0));
        int i = size * size - 1;
        for (; i > 0; i--)
            cells.add(0, new Cell(i));
        //scramble the cells
        int pos = cells.size() - 1;
        i = size == 3 ? 50 : size == 4 ? 180 : 400;
        Random rand = new Random();
        for (; i >= 0; i--){
            int row = pos / size;
            while (true) {
                int p = rand.nextInt(4);
                //up
                if (p == 0) {
                    if (pos - size >= 0) {
                        moveTile(pos - size);
                        pos -= size;
                        break;
                    }
                }
                //down
                if (p == 1) {
                    if (pos + size < cells.size()) {
                        moveTile(pos + size);
                        pos += size;
                        break;
                    }
                }
                //right
                if (p == 2) {
                    if ((pos + 1) / size == row && pos + 1 < cells.size()) {
                        moveTile(pos + 1);
                        pos += 1;
                        break;
                    }
                }
                //left
                if (p == 3) {
                    if ((pos - 1) / size == row && pos - 1 >= 0) {
                        moveTile(pos - 1);
                        pos -= 1;
                        break;
                    }
                }
            }
        }
    }

    public ArrayList<Cell> getCells(){
        return cells;
    }
    public int getScore(){
        return moves;
    }
    public boolean isGameOver() {
        return gameOver;
    }
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    public int getSize() {
        return size;
    }

    public boolean endMove(){
        //play click 1 because there was a move
        SoundManager.playClick(view.getActivity());

        moves++;
        finishMove();

        //check if gem is over
        gameOver = checkGameOver();
        return gameOver;
    }
    /**
     * Tries to move the clicked cell in any direction if there is an empty space around it
     * @param position The position of the clicked cell
     * @return <b>true</b> if the game is over, <b>false</b> if the game continues
     */
    public boolean clickCell(int position) {
        if (gameOver) return false;

        //move the tile
        if (!moveTile(position)) {
            //play click 2 because no move
            SoundManager.playClick2(view.getActivity());
            return false;
        }

        return endMove();
    }
    /**
     * Tries to move a cell in the selected direction
     * @param dir
     * @param position
     * @return
     */
    public boolean flingCell(int dir, int position){
        if (gameOver) return false;

        //check dir for empty cell
        boolean allowMove = false;
        if (dir == 0) { //up
            if (position - size >= 0 && cells.get(position - size).getNumber() == 0)
                allowMove = true;
        } else if (dir == 1) { //down
            if (position + size < cells.size() && cells.get(position + size).getNumber() == 0)
                allowMove = true;
        } else if (dir == 2) { //right
            if (position / size == (position + 1) / size && position + 1 < cells.size()
                    && cells.get(position + 1).getNumber() == 0)
                allowMove = true;
        } else if (dir == 3) { //left
            if (position / size == (position - 1) / size && position - 1 >= 0
                    && cells.get(position - 1).getNumber() == 0)
                allowMove = true;
        }

        //move the tile
        if (allowMove)
            moveTile(position);
        else {
            //play click 2 because no move
            SoundManager.playClick2(view.getActivity());
            return false;
        }

        return endMove();
    }
    public boolean flingBoard(int dir){
        if (gameOver) return false;

        //find the position of the empty cell
        int pos = 0;
        for (; pos < cells.size(); pos++)
            if (cells.get(pos).getNumber() == 0)
                break;

        //adjust position
        if (dir == 0){ //up
            if (pos + size < cells.size())
                pos += size;
        }
        if (dir == 1){ //down
            if (pos - size >= 0)
                pos -= size;
        }
        if (dir == 2){ //right
            if (pos / size == (pos - 1) / size && pos - 1 >= 0)
                pos -= 1;
        }
        if (dir == 3){ //left
            if (pos / size == (pos + 1) / size && pos + 1 < cells.size())
                pos += 1;
        }

        return clickCell(pos);
    }
    private boolean moveTile(int position){
        //check if there is an empty position so move it that way
        int row = position / size;
        //right
        if ((position + 1) / size == row && position + 1 < cells.size() && cells.get(position + 1).getNumber() == 0){
            cells.get(position + 1).setNumber(cells.get(position).getNumber());
            cells.get(position).setNumber(0);
        }
        //left
        else if ((position - 1) / size == row && position - 1 >= 0 && cells.get(position - 1).getNumber() == 0){
            cells.get(position - 1).setNumber(cells.get(position).getNumber());
            cells.get(position).setNumber(0);
        }
        //up
        else if (position - size >= 0 && cells.get(position - size).getNumber() == 0){
            cells.get(position - size).setNumber(cells.get(position).getNumber());
            cells.get(position).setNumber(0);
        }
        //down
        else if (position + size < cells.size() && cells.get(position + size).getNumber() == 0){
            cells.get(position + size).setNumber(cells.get(position).getNumber());
            cells.get(position).setNumber(0);
        }
        else
            return false;
        return true;
    }

    private boolean checkGameOver(){
        for (int i = 0; i < cells.size() - 1; i++){
            if (cells.get(i).getNumber() != i + 1) return false;
        }
        return true;
    }

    public void finishMove(){
        view.updateUI();
    }

    public String boardToString(){
        String b = "";
        if (!gameOver) {
            for (Cell c : cells)
                b += (b.length() > 0 ? "," : "") + c.getNumber();
            b = size + ":" + moves + ":" + b;
        }
        return b;
    }
    public void stringToBoard(String brds){
        try {
            if (brds.length() <= 0) return;
            String[] bs = brds.split(":");
            size = Integer.valueOf(bs[0]);
            moves = Integer.valueOf(bs[1]);
            String[] b = bs[2].split(",");
            cells.clear();
            for (String c : b) {
                cells.add(new Cell(Integer.valueOf(c)));
            }
        } catch (Exception ex){
            initiateBoard();
        }
        view.updateUI();
    }

    // Parcelling part
    public BoardController(Parcel in){
        cells = in.readArrayList(Cell.class.getClassLoader());
        gameOver = in.readInt() == 1;
        size = in.readInt();
        moves = in.readInt();
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(cells);
        dest.writeInt(gameOver ? 1 : 0);
        dest.writeInt(size);
        dest.writeInt(moves);
    }
    public static final Creator CREATOR = new Creator() {
        public BoardController createFromParcel(Parcel in) {
            return new BoardController(in);
        }

        public BoardController[] newArray(int size) {
            return new BoardController[size];
        }
    };

}
