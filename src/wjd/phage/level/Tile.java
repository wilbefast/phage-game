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

import java.io.Serializable;
import java.util.Iterator;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.amb.view.IVisible;
import wjd.math.Rect;
import wjd.math.V2;

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public class Tile implements IVisible, Serializable
{
  /* CONSTANTS */

  public static final V2 SIZE = new V2(32, 32);
  public static final V2 HSIZE = SIZE.clone().scale(0.5f);
  public static final V2 ISIZE = SIZE.clone().inv();

  /* NESTING */
  public static enum EType
  {

    FLOOR,
    WALL
  }

  public static class RowByRow implements Iterator<Tile>
  {
    // attributes
    private Tile current;
    private int max_col, max_row;
    
    // methods
    public RowByRow(Tile current, Tile max)
    {
      this.current = current;
      max_col 
        = (int)((max == null) ? current.tilegrid[0].length-1 : max.position.x);
      max_row 
        = (int)((max == null) ? current.tilegrid.length-1 : max.position.y);
    }
    public RowByRow(Tile _current)
    {
      this(_current, null);
    }

    @Override
    public boolean hasNext()
    {
      return (!((int)current.position.y == max_row
              && (int)current.position.x == max_col));
    }

    @Override
    public Tile next()
    {
      Tile previous = current;
      current = ((int)current.position.x == max_col
        ? current.tilegrid[(int)current.position.y+1][0]
        : current.tilegrid[(int)current.position.y][(int)current.position.x+1]);

      return previous;
    }

    @Override
    public void remove()
    {
      // do nothing
    }
    
  }
  
  public static class Field implements Iterable<Tile>
  {
    // global
    public static Field cache[] = { new Field(null, null) };
    
    // attributes
    public Tile first, last;
    
    // methods
    public Field(Tile first, Tile last)
    {
      this.first = first;
      this.last = last;
    }

    public void reset(Tile first, Tile last)
    {
      this.first = first;
      this.last = last;
    }
    
    // overrides -- object
    @Override
    public String toString()
    {
      return "[" + first + ',' + last + ']';
    }
    
    // implements -- iterable
    @Override
    public Iterator<Tile> iterator()
    {
      return new RowByRow(first, last);
    }
  }
  /* ATTRIBUTES */
  public final V2 position; // (col, row)
  private EType type;
  private Unit unit = null;
  public final Tile[][] tilegrid;

  /* METHODS */
  // constructors
  public Tile(int row, int col, EType type, Tile[][] tilegrid)
  {
    position = new V2(col, row);
    this.type = type;
    this.tilegrid = tilegrid;
  }

  // accessors
  public Unit getUnit()
  {
    return unit;
  }

  // mutators
  public void setType(EType type)
  {
    this.type = type;
  }

  public void setUnit(Unit unit)
  {
    this.unit = unit;
  }

  /* OVERRIDES -- IDYNAMIC */
  @Override
  public void render(ICanvas canvas)
  {
    // draw tile -- setup context
    canvas.setColour(type == EType.FLOOR ? Colour.RED : Colour.BLUE);

    // draw tile -- background
    V2.cache[0].reset(position).scale(SIZE);
    V2.cache[1].reset(SIZE).scale(canvas.getCamera().getZoom() + 1);
    Rect.cache[0].reset(V2.cache[0], V2.cache[1]);
    canvas.box(Rect.cache[0], true);

    // draw tile -- unit (optional)
    if (unit != null)
      unit.render(canvas);
  }
  
  /* OVERRIDES -- OBJECT */
  @Override
  public String toString()
  {
    return type + " at " + position + (unit == null ? "" : " contains " + unit);
  }
}
