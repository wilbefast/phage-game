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
import wjd.amb.view.ICanvas;
import wjd.amb.view.IVisible;
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
  /* CLASS NAMESPACE FUNCTIONS */
  
  public static Unit load(Tile tile,  ObjectInputStream in) 
  throws IOException, ClassNotFoundException
  {
    // create an object of the correct type
    Unit.Type t = (Unit.Type )in.readObject();
    return fromType(t, tile);
  }
  
  public static Unit fromType(Unit.Type type, Tile tile)
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
  
  public static enum Type
  {
    MACROPHAGE,
    CIVILLIAN_CELL,
    INFECTED_CELL,
    LYMPHOCYTE_B,
    LYMPHOCYTE_T,
    LYMPHOCYTE_B_MEMORY,
    LYMPHOCYTE_T_MEMORY,
    LYMPHOCYTE_B_EFFECTOR,
    LYMPHOCYTE_T_EFFECTOR
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
  
  public abstract boolean isControllable();
  
  public abstract void renderOrder(ICanvas canvas);
  
  public abstract Type getType();
}
