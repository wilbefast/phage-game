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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import wjd.math.Rect;
import wjd.math.V2;

/**
 *
 * @author wdyce
 * @since Nov 9, 2012
 */
public class TileGrid implements Iterable<Tile>
{
  /* ATTRIBUTES */
  
  public Tile[][] tiles;
  private final Rect grid_area;
  private final Rect pixel_area;
  
  /* METHODS */

  // constructors
  TileGrid(V2 size)
  {
    grid_area = new Rect(V2.ORIGIN, size).floor();
    pixel_area = grid_area.clone().stretch(Tile.SIZE).floor();
    tiles = new Tile[(int)size.y][(int)size.x];
  }

  private TileGrid(Tile[][] tiles, Rect pixel_area)
  {
    this.tiles = tiles;
    Rect max_grid_area = new Rect(0, 0, tiles[0].length-1, tiles.length-1);
    this.grid_area = 
      pixel_area.clone().div(Tile.SIZE).getIntersection(max_grid_area).floor();
    this.pixel_area = grid_area.clone().stretch(Tile.SIZE).floor();
  }
  
  // mutators
  
  /**
   * Set all the tiles in the grid to be free.
   */
  public TileGrid clear()
  {
    // set all tiles as free
    for (int row = (int)grid_area.y; row < (int)(grid_area.y+grid_area.h); row++)
      for (int col = (int)grid_area.x; col < (int)(grid_area.x+grid_area.w); col++)
        tiles[row][col] = new Tile(row, col, Tile.EType.FLOOR, tiles);
    return this;
  }
    
  // accessors
  /** Which cells of the grid are inside the rectangle?
   * 
   * @param sub_area the rectangle which we want to draw cells from.
   * @return a Tile.Field structure containing a pair of 
   * coordinates corresponding to the top-left- and bottom-right-most cells in
   * the grid.
   */
  public TileGrid createSubGrid(Rect sub_area)
  {
    // check for invalid rectangles
    /*if(sub_area.endx() < Tile.SIZE.x*grid_area.x || sub_area.endy() < Tile.SIZE.y*grid_area.y 
       || sub_area.x > Tile.SIZE.x*grid_area.w || sub_area.y > Tile.SIZE.y*grid_area.h)
        return null;*/
    
    // grab the border indices
    /*int min_col = (int)Math.max(0, sub_area.x*Tile.ISIZE.x-1),
        min_row = (int)Math.max(0, sub_area.y*Tile.ISIZE.y-1),
        max_col = (int)Math.min(grid_area.w-1, sub_area.endx()*Tile.ISIZE.x+1),
        max_row = (int)Math.min(grid_area.h-1, sub_area.endy()*Tile.ISIZE.y+1);*/

    // build and return the field
    
    Rect r = sub_area.getIntersection(pixel_area);
    
    return new TileGrid(tiles, r);//sub_area.getIntersection(pixel_area));
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
  
  /* OVERRIDES -- OBJECT */
  
  @Override
  public String toString()
  {
    return "Tilegrid(" + grid_area + ')';
  }
  
  /* IMPLEMENTS -- ITERABLE */
  
  public static class RowByRow implements Iterator<Tile>
  {
    // attributes
    private Tile current;
    private int min_col, min_row, max_col, max_row;
    
    // methods
    public RowByRow(Tile current, Tile max)
    {
      this.current = current;
      min_col = (int)current.grid_position.x;
      min_row = (int)current.grid_position.y;
      max_col 
        = (int)((max == null) ? current.tilegrid[0].length-1 : max.grid_position.x);
      max_row 
        = (int)((max == null) ? current.tilegrid.length-1 : max.grid_position.y);
    }
    public RowByRow(Tile _current)
    {
      this(_current, null);
    }

    @Override
    public boolean hasNext()
    {
      return (current != null);
    }

    @Override
    public Tile next()
    {
      Tile previous = current;

      current = ((int)current.grid_position.x == max_col
        ? (((int)current.grid_position.y == max_row) 
              ? null 
              : current.tilegrid[(int)current.grid_position.y+1][min_col]) 
        : current.tilegrid[(int)current.grid_position.y][(int)current.grid_position.x+1]);

      return previous;
    }

    @Override
    public void remove()
    {
      // do nothing
    }
  }

  @Override
  public Iterator<Tile> iterator()
  {
    return new RowByRow(tiles[(int)grid_area.y][(int)grid_area.x], 
                        tiles[(int)grid_area.endy()][(int)grid_area.endx()]);
  }
}
