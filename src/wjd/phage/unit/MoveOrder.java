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

import java.util.Deque;
import java.util.LinkedList;
import wjd.amb.control.EUpdateResult;
import wjd.amb.view.Colour;
import wjd.amb.view.ICanvas;
import wjd.math.V2;
import wjd.phage.level.Tile;
import wjd.phage.pathing.PathSearch;
import wjd.util.Timer;

/**
 *
 * @author wdyce
 * @since Dec 2, 2012
 */
public class MoveOrder extends AUnitOrder
{
  /* CONSTANTS */
  private static final int BLOCKED_TIMEOUT = 1000;    // ms
  private static final int REPATH_INTERVAL = 3000;    // ms
  private static final int ORDER_TIMEOUT = 15000;  // ms
  
  /* ATTRIBUTES */
  private Deque<Tile> path = new LinkedList<Tile>();
  private Timer blocked_repath = new Timer(BLOCKED_TIMEOUT);
  private Timer blocked_cancel = new Timer(ORDER_TIMEOUT);
  private Timer repath_timer = new Timer(REPATH_INTERVAL);
  private Tile destination;

  /* METHODS */
  public MoveOrder(Unit owner, Tile destination)
  {
    super(owner);
    this.destination = destination;
    recalculatePath();
  }

  /* IMPLEMENTS -- IVISIBLE */
  @Override
  public void render(ICanvas canvas)
  {
    canvas.setColour(Colour.YELLOW);
    V2 start = new V2(), end = new V2();
    start.reset(owner.position);
    for(Tile t : path)
    {
      end.reset(t.grid_position).scale(Tile.SIZE).add(Tile.HSIZE);
      canvas.line(start, end);
      start.reset(end);
    }
    end.reset(destination.grid_position).scale(Tile.SIZE).add(Tile.HSIZE);
    canvas.circle(end, Tile.HSIZE.x, true);
    canvas.setCameraActive(false);
  }

  /* IMPLEMENTS -- IDYNAMIC */
  @Override
  public EUpdateResult update(int t_delta)
  {
    // 0. repath periodically in case a better path has freed itself
    if(repath_timer.update(t_delta) == EUpdateResult.FINISHED)
      return recalculatePath();
    
    // 1. get a new tile from the path if there is one
    if(owner.next_tile == null)
    {
      if(!path.isEmpty())
        owner.next_tile = path.pop(); 
      else
      {
        // have we arrived at our destination?
        if(owner.tile == destination)
        {
          owner.order = null;
          return EUpdateResult.FINISHED;
        }
        // if not wait a few seconds and then try again
        return wait(t_delta);
      }
    }
    
    // 2. enter this next tile if it is unobstructed
    if(owner.progress.isEmpty())
    {
      // move into a new tile...
      if(!owner.next_tile.unitStartEnter(owner))
        return wait(t_delta);

      // ... and out of the current one
      owner.tile.setUnit(null);
    }

    // 3. gradually move into this newt tile
    owner.progress.tryDeposit((float)t_delta * owner.getMoveSpeed());
    owner.position.inter(owner.tile.pixel_position, 
                          owner.next_tile.pixel_position, 
                          owner.progress.balance()).add(Tile.HSIZE);

    // 4. when entirely inside the new tile, set this to our current tile
    if(owner.progress.isFull())
    {
      // move to the new position
      owner.progress.empty();
      owner.tile = owner.next_tile;
      owner.next_tile = null;
      // reset the "boredom" timers
      blocked_repath.empty();
      blocked_cancel.empty();
      // tell the Tile to move the owner to another Tile
      return EUpdateResult.MOVE_ME;
    }
    
    // 5. rinse and repeat
    return EUpdateResult.CONTINUE;
  }
  
  /* SUBROUTINES */
  
  private EUpdateResult wait(int t_delta)
  {
    // cancel order after a long wait
    if(blocked_cancel.update(t_delta) == EUpdateResult.FINISHED)
    {
      owner.order = null;
      return EUpdateResult.CANCEL;
    }
    // recalculate path after a short wait
    else if(blocked_repath.update(t_delta) == EUpdateResult.FINISHED)
      return recalculatePath();
    else
      return EUpdateResult.BLOCKED;
  }
  
  private EUpdateResult recalculatePath()
  {
    if(owner.next_tile != null && owner.progress.isEmpty())
    {
      owner.next_tile = null;
      return EUpdateResult.CANCEL;
    }
    
    Tile source = (owner.next_tile != null) ? owner.next_tile : owner.tile;
    new PathSearch(source, destination).writePath(path);
    // reset the "boredom" timers
    blocked_repath.empty();
    blocked_cancel.empty();
    
    return EUpdateResult.CONTINUE;
  }
}
