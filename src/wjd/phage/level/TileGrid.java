/*
 Copyright (C) 2012 William James Dyce

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wjd.phage.level;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import wjd.math.Rect;
import wjd.math.V2;

/**
 *
 * @author wdyce
 * @since Nov 9, 2012
 */
public class TileGrid 
{
  /* ATTRIBUTES */
  public Tile[][] tiles;
  private final V2 size;
  
  /* METHODS */

  // constructors
  TileGrid(V2 size)
  {
    this.size = size;
    tiles = new Tile[(int)size.y][(int)size.x];
  }
  
  // mutators
  
  /**
   * Set all the tiles in the grid to be free.
   */
  public TileGrid clear()
  {
    // set all tiles as free
    for (int row = 0; row < tiles.length; row++)
      for (int col = 0; col < tiles[row].length; col++)
        tiles[row][col] = new Tile(row, col, Tile.EType.FLOOR, tiles);
    return this;
  }
    
  // accessors
  /** Which cells of the grid are inside the rectangle?
   * 
   * @param rect the rectangle which we want to draw cells from.
   * @return a Tile.Field structure containing a pair of 
   * coordinates corresponding to the top-left- and bottom-right-most cells in
   * the grid.
   */
  public Tile.Field rectToCells(Rect rect)
  {
    // check for invalid rectangles
    if(rect.endx() < 0 || rect.endy() < 0 
       || rect.x > Tile.SIZE.x*size.x || rect.y > Tile.SIZE.y*size.y)
        return null;
    
    // grab the border indices
    int min_col = (int)Math.max(0, rect.x*Tile.ISIZE.x-1),
        min_row = (int)Math.max(0, rect.y*Tile.ISIZE.y-1),
        max_col = (int)Math.min(size.x-1, rect.endx()*Tile.ISIZE.x+1),
        max_row = (int)Math.min(size.y-1, rect.endy()*Tile.ISIZE.y+1);

    // build and return the field
    return new Tile.Field(tiles[min_row][min_col], tiles[max_row][max_col]);
  }
  
  /** 
   * Check if a position is on the grid.
   * 
   * @param grid_pos the vector pair of coordinates (col, row) to check.
   * @return true is the given pair of coordinates is inside the grid, false if
   * not.
   */
  public boolean validGridPos(V2 grid_pos)
  {
    return (grid_pos.x >= 0 && grid_pos.y >= 0 
           && grid_pos.y < tiles.length && grid_pos.x < tiles[0].length);
  }
  
  // load and save
  
  public TileGrid load(File file)
  {
    try
    {
        Object o 
          = (new ObjectInputStream(new FileInputStream(file))).readObject();
        
        if(o instanceof Tile[][])
          this.tiles = (Tile[][])o;
        else
          throw new ClassNotFoundException();
    }
    catch (FileNotFoundException ex)
    {
      Logger.getLogger(LevelScene.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IOException ex)
    {
      Logger.getLogger(LevelScene.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (ClassNotFoundException ex)
    {
      Logger.getLogger(LevelScene.class.getName()).log(Level.SEVERE, null, ex);
    }
    finally
    {
      return this;
    }
  }
  
  public TileGrid save(File file)
  {
    try
    {
      // open specified file and write the object
      (new ObjectOutputStream(new FileOutputStream(file))).writeObject(tiles);
    }
    catch (FileNotFoundException ex)
    {
      Logger.getLogger(LevelScene.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IOException ex)
    {
      Logger.getLogger(LevelScene.class.getName()).log(Level.SEVERE, null, ex);
    }
    finally
    {
      return this;
    }
  }
}
