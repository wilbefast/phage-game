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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IDynamic;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.amb.view.IVisible;
import wjd.math.Rect;
import wjd.math.V2;
import wjd.phage.unit.Unit;

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public class Tile implements IVisible, IDynamic
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

  /* ATTRIBUTES */
  public final TileGrid grid;
  public final V2 grid_position; // (col, row)
  private final Rect pixel_area;
  private EType type;
  private Unit unit = null;

  /* METHODS */
  
  // constructors
  public Tile(int row, int col, EType type, TileGrid grid)
  {
    grid_position = new V2(col, row);
    pixel_area = new Rect(grid_position.clone().scale(SIZE), SIZE);
    this.type = type;
    this.grid = grid;
  }
  
  public Tile(ObjectInputStream in, TileGrid grid) throws IOException, ClassNotFoundException
  {
    grid_position = (V2)in.readObject();
    pixel_area = (Rect)in.readObject();
    type = (EType)in.readObject();
    if((Boolean)in.readObject())
      unit = new Unit(this, in);
    
    this.grid = grid;
  }

  // accessors
  public Unit getUnit()
  {
    return unit;
  }
  
  public EType getType()
  {
    return type;
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
  
  
  public void save(ObjectOutputStream out) throws IOException 
  {
    // don't write the grid, or we'll end up with a recursion loop!
    out.writeObject(grid_position);
    out.writeObject(pixel_area);
    out.writeObject(type);
    out.writeObject(unit != null);
    if(unit != null)
      unit.save(out);
  }

  /* OVERRIDES -- IDYNAMIC */
  @Override
  public void render(ICanvas canvas)
  {
    // draw tile -- setup context
    canvas.setColour(type == EType.FLOOR ? Colour.RED : Colour.BLUE);

    // draw tile -- background
    canvas.box(pixel_area, true);
    
    // draw tile -- unit (optional)
    if (unit != null)
      unit.render(canvas);
  }
  
  /* OVERRIDES -- OBJECT */
  @Override
  public String toString()
  {
    return type + " at " + grid_position + (unit == null ? "" : " contains " + unit);
  }
  
  /* IMPLEMENTS -- IDYNAMIC */
 
  @Override
  public EUpdateResult update(int t_delta)
  {
    // update the unit if there is one
    if(unit != null)
    {
      EUpdateResult result = unit.update(t_delta);
      // delete if required
      if(result == EUpdateResult.DELETE_ME)
        unit = null;
    }
    
    // all clear
    return EUpdateResult.CONTINUE;
  }
}
