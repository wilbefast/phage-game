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
  
  public static final Colour C_WALL = new Colour(97, 0, 21);
  public static final Colour C_FOG = new Colour(55, 55, 55);

  /* NESTING */
  public static enum EType
  {

    FLOOR,
    WALL
  }

  /* ATTRIBUTES */
  public final TileGrid grid;
  public final V2 grid_position, pixel_position;
  public final Rect pixel_area;
  private EType type;
  private Unit unit = null, unit_inbound = null;
  private Infection infection = new Infection(this);
  private boolean visible = false;
  private char[] neighbours = { 0, 0, 0, 0 };
  
  /* METHODS */
  
  // constructors
  public Tile(int row, int col, EType type, TileGrid grid)
  {
    grid_position = new V2(col, row);
    pixel_position = grid_position.clone().scale(SIZE);
    pixel_area = new Rect(pixel_position, SIZE);
    this.type = type;
    this.grid = grid;
  }
  
  public Tile(ObjectInputStream in, TileGrid grid) throws IOException, ClassNotFoundException
  {
    // retrieve grid position and deduce pixel position and area
    grid_position = (V2)in.readObject();
    pixel_position = grid_position.clone().scale(SIZE);
    pixel_area = new Rect(pixel_position, SIZE);

    type = (EType)in.readObject();
    
    infection = new Infection(in, this);
    
    // read unit if unit is present to be read
    if((Boolean)in.readObject()) 
      unit = Unit.load(this, in);
    
    this.grid = grid;
  }

  // accessors
  public Unit getUnit()
  {
    return (unit != null) ? unit : unit_inbound;
  }
  
  public EType getType()
  {
    return type;
  }
  
  public boolean isPathable()
  {
    return (type == EType.FLOOR && unit == null && unit_inbound == null);
  }
  
  public Infection getInfection()
  {
    return infection;
  }
  
  // mutators
  
  public void refreshNeighbourHash()
  {
    /*Each of the 4 corners is given a seperate hash value between 0 and 4. We
    only pay attention to the corners if the two adjascent sides are equal to
    the center:

    |--|    |-x|
    |X-| == |X-| == 0 (binary 00)

    |x-|    |xx|
    |X-| == |X-| == 1 (binary 01)

    |--|    |-x|
    |Xx| == |Xx| == 2 (binary 10)

    |xx|
    |Xx| == 4 (binary 11)

    Corner are evaluated in this order:
    0|x-| 1|--| 2|-x| 3|--|
     |--|  |x-|  |--|  |-x|
    */

    V2 pos = new V2();
    
    for(int d_col = -1, corner = 0; d_col < 2; d_col += 2)
    for(int d_row = -1; d_row < 2; d_row += 2, corner++)
    {
      // reset hash
      neighbours[corner] = 0;
      
      // delta along the vertical axis
      pos.xy(grid_position.x, grid_position.y + d_row);
      if(grid.validGridPos(pos) && grid.gridToTile(pos).type == type)
        neighbours[corner] += 1;
      
      // delta along the horizontal axis
      pos.xy(grid_position.x + d_col, grid_position.y);
      if(grid.validGridPos(pos) && grid.gridToTile(pos).type == type)
        neighbours[corner] += 2;
      
      // delta along both axes if both sides are of the same type as the center
      pos.xy(grid_position.x + d_col, grid_position.y + d_row);
      if(neighbours[corner] == 3 
      && grid.validGridPos(pos) && grid.gridToTile(pos).type == type)
        neighbours[corner] = 4;
    }
  }
  
  public void setType(EType type)
  {
    // reset the type
    this.type = type;
    if(type == EType.WALL)
      unit = null;
    if(type != EType.FLOOR)
      infection.empty();
    
    // recalculate the hash
    Iterable<Tile> n = grid.getNeighbours(this, true);
    for(Tile t : n)
      t.refreshNeighbourHash();
  }
  
  public void setUnit(Unit new_unit)
  {
    if(unit == null || new_unit == null)
      unit = new_unit;
  }

  public boolean unitStartEnter(Unit u)
  {
    // tile cannot be entered while someone else is present, entering or leaving
    if(unit != null || unit_inbound != null)
      return false;
    
    // the supplicant is now the inbound unit
    unit_inbound = u;
    return true;
  }
  
  public void unitCancelEnter(Unit u)
  {
    if(unit_inbound == u)
      unit_inbound = null;
  }

  public void unitFinishEnter(Unit u)
  {
    if(unit_inbound == u)
    {
      // the inbound unit is now the present unit
      unit = unit_inbound;
      unit_inbound = null;
    }
  }

  public void save(ObjectOutputStream out) throws IOException 
  {
    // don't write pixel position or area, as these can be deduced
    out.writeObject(grid_position);

    out.writeObject(type);
    
    infection.save(out);
  
    // write a boolean to signify if unit is present or not
    out.writeObject(unit != null);
    if(unit != null)
      unit.save(out);
    
    // don't write the grid, or we'll end up with a recursion loop!
  }

  /* OVERRIDES -- IDYNAMIC */
  @Override
  public void render(ICanvas canvas)
  {
    // walls
    if(type == EType.WALL)
    {
      canvas.setColour(C_WALL);
      canvas.box(pixel_area, true);
    }
    
    // units (optional)
    if (unit != null)
      unit.render(canvas);
    if (unit_inbound != null)
      unit_inbound.render(canvas);
    
    // infection (optional)
    infection.render(canvas);
    
    // black mask
    if(!visible)
    {
      canvas.setColour(C_FOG);
      canvas.box(pixel_area, false);
    }
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
      // replace if required
      if(result == EUpdateResult.REPLACE_ME)
        unit = unit.getReplacement();
    }
    
    // update the inbound unit if there is one
    if(unit_inbound != null)
    {
      EUpdateResult result = unit_inbound.update(t_delta);
      // delete if required
      if(result == EUpdateResult.DELETE_ME)
        unit_inbound = null;
    }
    
    // update the infection
    infection.update(t_delta);
    
    // all clear
    return EUpdateResult.CONTINUE;
  }
}
