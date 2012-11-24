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

/**
 *
 * @author wdyce
 * @since Nov 1, 2012
 */
public class Unit implements IVisible, IDynamic, Serializable
{
  /* ATTRIBUTES */
  private Tile current, next = null;
  private float progress = 0.0f;
  private boolean selected = false;
  private UnitOrder order = null;
  
  private int state = 0;
  
  //private AUnitState state = AUnitState.IDLING;
  private Deque<Tile> path;
  
  /* METHODS */

  // constructors
  public Unit(Tile tile)
  {
    this.current = tile;
  }
  
  public Unit(Tile tile, ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    selected = (Boolean)in.readObject();
    //order = (UnitOrder)in.readObject();
    order = null;
    state = 0;
    path = null;
    
    this.current = tile;
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

  // mutators
  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }
  
  public void setOrder(UnitOrder order)
  {
    this.order = order;
    path = null;
    state = 0;
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
      start.reset(current.grid_position).scale(Tile.SIZE);
      for(Tile t : path)
      {
        end.reset(t.grid_position).scale(Tile.SIZE);
        canvas.line(start, end);
        start.reset(end);
      }
    canvas.setCameraActive(false);
  }
  
  /* IMPLEMENTS -- IVISIBLE */
  private static V2 stamp_pos = new V2();
  @Override
  public void render(ICanvas canvas)
  {
    canvas.setColour(selected ? Colour.WHITE : Colour.BLACK);
    
    stamp_pos.reset(current.grid_position).scale(Tile.SIZE).add(Tile.HSIZE);
    canvas.circle(stamp_pos, Tile.SIZE.x/2, true);
  }
  
  /* IMPLEMENTS -- IDYNAMIC */

  @Override
  public EUpdateResult update(int t_delta)
  {
    // pass update to state
    /*AUnitState next_state = state.update(this, t_delta);
    if(next_state != null)
      state = next_state;*/
    
    
    //! IDLE
    if(state == 0 && order != null)
    {
      PathSearch search = new PathSearch(current, order.target);
      state = 1;
      search.run();
      path = search.getPath();
    }
    
    //! MOVING
    /*if(state == 1)
    {
      if(next == null)
      {
        next = path.pop();
      }
      else
      {
        progress += t_delta/3000;
      }
    }*/
   
    // All clear
    return EUpdateResult.CONTINUE;
  }
}
