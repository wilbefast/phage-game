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
  private static final int PATH_TIMEOUT_DURATION = 3000; // ms
  
  /* ATTRIBUTES */
  private Deque<Tile> path;
  private Timer path_timeout = new Timer(PATH_TIMEOUT_DURATION);
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
    canvas.setCameraActive(true);
    canvas.setColour(Colour.YELLOW);
    V2 start = new V2(), end = new V2();
    start.reset(owner.position);
    for(Tile t : path)
    {
      end.reset(t.grid_position).scale(Tile.SIZE).add(Tile.HSIZE);
      canvas.line(start, end);
      start.reset(end);
    }
    canvas.setCameraActive(false);
  }

  /* IMPLEMENTS -- IDYNAMIC */
  @Override
  public EUpdateResult update(int t_delta)
  {
    // 1. get a new tile from the path if there is one
    if(owner.next_tile == null || owner.next_tile == owner.tile)
    {
      if(!path.isEmpty())
        owner.next_tile = path.pop(); 
      else
      {
        owner.order = null;
        return EUpdateResult.FINISHED;
      }
    }
    
    // 2. enter this next tile if it is unobstructed
    if(owner.progress.isEmpty())
    {
      // move into a new tile...
      if(!owner.next_tile.unitTryStartEnter(owner))
      {
        //if(path_timeout.update(t_delta) == EUpdateResult.FINISHED)
          recalculatePath();
        return EUpdateResult.BLOCKED;
      }

      // ... and out of the current one
      owner.tile.unitStartExit();
    }

    // 3. gradually move into this newt tile
    owner.progress.tryDeposit((float)t_delta/500.0f);
    float p = owner.progress.balance();
    V2 src = owner.tile.pixel_position, dest = owner.next_tile.pixel_position;
    owner.position.x = (1-p)*src.x + p*dest.x + Tile.HSIZE.x;
    owner.position.y = (1-p)*src.y + p*dest.y + Tile.HSIZE.y;

    /*position = V2.inter(tile.pixel_position, destination.pixel_position,
                                             progress.balance());*/

    // 4. when entirely inside the new tile, set this to our current tile
    if(owner.progress.isFull())
    {
      owner.tile.unitFinishExit();
      owner.next_tile.unitFinishEnter();
      owner.progress.empty();
      owner.tile = owner.next_tile;
      owner.next_tile = null;
    }
    
    // 5. rinse and repeat
    return EUpdateResult.CONTINUE;
  }
  
  /* SUBROUTINES */
  private void recalculatePath()
  {
    Tile source = (owner.next_tile != null) ? owner.next_tile : owner.tile;
    path = new PathSearch(source, destination).getPath();
  }
}
