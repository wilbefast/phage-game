/*
 Copyright (C) 2013 William James Dyce

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
package wjd.phage.unit;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import wjd.amb.control.IDynamic;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.amb.view.IVisible;
import wjd.math.Circle;
import wjd.math.V2;
import wjd.phage.level.Tile;
import wjd.util.BoundedValue;

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public abstract class Unit implements IVisible, IDynamic, Serializable
{
  /* CONSTANTS */
  
  public static final float DEFAULT_MOVE_SPEED = 0.002f;
  
  /* CLASS NAMESPACE FUNCTIONS */
  
  public static Unit load(Tile tile,  ObjectInputStream in) 
  throws IOException, ClassNotFoundException
  {
    // create an object of the correct type
    Unit.EType t = (Unit.EType )in.readObject();
    return fromType(t, tile);
  }
  
  public static Unit fromType(Unit.EType type, Tile tile)
  {
    switch(type)
    {
      case MACROPHAGE:
        return new Macrophage(tile);
        
      case CIVILLIAN_CELL:
        return new CivillianCell(tile);
        
      case INFECTED_CELL:
        return new InfectedCell(tile);
        
        
        // ... etc
        
        
      default:
        return null;
    }
  }
  
  
  /* NESTING */
  
  public static enum EType
  {    
    MACROPHAGE(Colour.WHITE),
    CIVILLIAN_CELL(Colour.RED),
    INFECTED_CELL(Colour.VIOLET),
    LYMPHOCYTE_B(Colour.GREEN),
    LYMPHOCYTE_T(Colour.YELLOW),
    LYMPHOCYTE_B_MEMORY(Colour.GREEN),
    LYMPHOCYTE_T_MEMORY(Colour.YELLOW),
    LYMPHOCYTE_B_EFFECTOR(Colour.GREEN),
    LYMPHOCYTE_T_EFFECTOR(Colour.YELLOW);
    
    public final Colour colour;
    
    private EType(Colour colour_)
    {
      this.colour = colour_;
    }
  }
  
  /* ATTRIBUTES */
  Tile tile, next_tile = null;
  V2 position;
  BoundedValue progress = new BoundedValue(1.0f);
  public boolean selected = false;
  AUnitOrder order = null;
  
  /* METHODS */

  // constructors
  public Unit(Tile tile)
  {
    this.tile = tile;
    position = tile.pixel_position.clone().add(Tile.HSIZE);
  }
  
  // accessors
  
  public float getMoveSpeed()
  {
    // override me
    return DEFAULT_MOVE_SPEED;
  }
  
  public Circle getSight()
  {
    // overrides me
    return null;
  }
  
  public Unit getReplacement()
  {
    // override me
    return null;
  }
  
  public boolean playerControlled()
  {
    // override me
    return false;
  }
  
  public void renderOverlay(ICanvas canvas)
  {
    // override me
  }
  
  // mutators
  
  public void setOrder(AUnitOrder new_order)
  {
    order = new_order;
    if(progress.isEmpty())
      next_tile = null;
  }
  
  public void save(ObjectOutputStream out) throws IOException
  {  
    out.writeObject(getType());
    
    // don't write the tile, or we'll end up with a recursion loop!
  }
  
  /* INTERFACE */
  
  public abstract EType getType();
}
