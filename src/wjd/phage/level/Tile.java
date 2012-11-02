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
  
  /* ATTRIBUTES */
  private V2 position;
  private EType type;
  private Unit unit = null;
  
  /* METHODS */

  // constructors
  public Tile(int row, int col, EType type)
  {
    position = new V2(col, row);
    this.type = type;
  }

  // accessors
  public V2 getPosition()
  {
    return position;
  }
  
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
  
  // update

  
  private static Rect stamp = new Rect();
  private static V2 stamp_pos = new V2(), stamp_size = new V2();
  @Override
  public void render(ICanvas canvas)
  {
    // draw tile -- setup context
    canvas.setColour(type == EType.FLOOR ? Colour.RED : Colour.BLUE);
    
    // draw tile -- background
    stamp_pos.reset(position).scale(SIZE);
    stamp_size.reset(SIZE).scale(canvas.getCamera().getZoom()+1);
    stamp.reset(stamp_pos, stamp_size);
    canvas.box(stamp);
    
    // draw tile -- unit (optional)
    if(unit != null)
      unit.render(canvas);
  }
}
