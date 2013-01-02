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
package wjd.phage.unit;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IDynamic;
import wjd.amb.view.Colour;
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
public class Unit implements IVisible, IDynamic, Serializable
{
  /* CONSTANTS */
  
  public static final Colour C = new Colour(173, 196, 206);
  
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
  
  public Unit(Tile tile, ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    this(tile);
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
    // don't write the tile, or we'll end up with a recursion loop!
  }
  
  public void renderOrder(ICanvas canvas)
  {
    // draw the order the unit is following (eg. its path) where applicable
    if(order != null)
      order.render(canvas);
  }

  
  /* IMPLEMENTS -- IVISIBLE */
  @Override
  public void render(ICanvas canvas)
  {        
    canvas.setColour(C);
    canvas.circle(position, Tile.SIZE.x/2, true);
    
    if(selected)
    {
      canvas.setLineWidth(3.0f);
      canvas.setColour(Colour.YELLOW);
      canvas.circle(position, Tile.SIZE.x/2, false);
    }
  }
  
  /* IMPLEMENTS -- IDYNAMIC */

  @Override
  public EUpdateResult update(int t_delta)
  {    
    // execute order
    if(order != null)
      order.update(t_delta);
    
    // clear up infection
    for(Tile t : tile.grid.getNeighbours(tile, true))
      t.getInfection().empty();
    
    // All clear
    return EUpdateResult.CONTINUE;

  }
}
