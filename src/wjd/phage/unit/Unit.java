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
import java.util.Deque;
import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IDynamic;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.amb.view.IVisible;
import wjd.math.V2;
import wjd.phage.level.Tile;
import wjd.phage.pathing.PathSearch;
import wjd.util.BoundedValue;

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public class Unit implements IVisible, IDynamic, Serializable
{
  /* ATTRIBUTES */
  private Tile tile, destination = null;
  private V2 position;
  private BoundedValue progress = new BoundedValue(1.0f);
  private boolean selected = false;
  private UnitOrder order = null;
  
  private int state = 0;
  
  //private AUnitState state = AUnitState.IDLING;
  private Deque<Tile> path;
  
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
    
    //selected = (Boolean)in.readObject();
    //order = (UnitOrder)in.readObject();
    order = null;
    state = 0;
    path = null;
  }

  // accessors
  public boolean isSelected() 
  { 
    return selected; 
  }
  
  public UnitOrder nextOrder()
  {
    return order;
  }
  
  public Tile getDestination()
  {
    return destination;
  }

  // mutators
  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }
  
  public void setOrder(UnitOrder new_order)
  {
    order = new_order;
    destination = null;
    progress.empty();
    path = null;
    state = 0;
  }
  
  public void setTile(Tile tile)
  {
    // skip if this is already the tile
    if(this.tile == tile)
      return;
    
    // remove form old tile and place in the new one
    this.tile.setUnit(null);
    this.tile = tile;
    position = tile.pixel_position.clone().add(Tile.HSIZE);
    
    // arrived at destination
    if(destination == tile)
    {
      destination = null;
      progress.empty();
    }
  }
  
  public void save(ObjectOutputStream out) throws IOException
  {
    // don't write the tile, or we'll end up with a recursion loop!
  }
  
  public void renderPath(ICanvas canvas)
  {
    // you can't draw what you don't have
    if(path == null)
      return;
    
    canvas.setCameraActive(true);
      canvas.setColour(Colour.YELLOW);
      V2 start = new V2(), end = new V2();
      start.reset(tile.grid_position).scale(Tile.SIZE);
      for(Tile t : path)
      {
        end.reset(t.grid_position).scale(Tile.SIZE);
        canvas.line(start, end);
        start.reset(end);
      }
    canvas.setCameraActive(false);
  }
  
  /* IMPLEMENTS -- IVISIBLE */
  @Override
  public void render(ICanvas canvas)
  {
    canvas.setColour(selected ? Colour.TEAL : Colour.BLACK);
    canvas.circle(position, Tile.SIZE.x/2, true);
  }
  
  /* IMPLEMENTS -- IDYNAMIC */

  @Override
  public EUpdateResult update(int t_delta)
  {
    // pass update to state
    /*AUnitState next_state = state.update(this, t_delta);
    if(next_state != null)
      state = next_state;*/
    
    // clear up infection
    for(Tile t : tile.grid.getNeighbours4(tile, Tile.EType.FLOOR))
      t.getInfection().empty();
    
    //! IDLE
    if(state == 0 && order != null)
    {
      PathSearch search = new PathSearch(tile, order.target);
      state = 1;
      search.run();
      path = search.getPath();
    }
    
    //! MOVING
    else if(state == 1)
    {
      // get a new destination
      if(destination == null)
      {
        if(path.isEmpty())
        {
          state = 0;
          path = null;
        }
        else
          destination = path.pop();
      }
      else
      {
        // move towards destination if it is free
        if(destination.getUnit() == null)
        {
          progress.tryDeposit((float)t_delta/500.0f);

          float p = progress.balance();
          V2 src = tile.pixel_position, dest = destination.pixel_position;
          position.x = (1-p)*src.x + p*dest.x + Tile.HSIZE.x;
          position.y = (1-p)*src.y + p*dest.y + Tile.HSIZE.y;

          /*position = V2.inter(tile.pixel_position, destination.pixel_position,
                                                   progress.balance());*/


          if(progress.isFull())
            return EUpdateResult.MOVE_ME;
        }
      }
    }
   
    // All clear
    return EUpdateResult.CONTINUE;
  }
}
