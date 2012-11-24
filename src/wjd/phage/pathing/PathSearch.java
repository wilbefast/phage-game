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
package wjd.phage.pathing;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import wjd.phage.level.Tile;
import wjd.phage.level.TileGrid;

/**
 *
 * @author wdyce
 * @since 16 Feb, 2012
 */
public class PathSearch implements Runnable
{
  /* NESTING */
  
  /* ATTRIBUTES */
  private final TileGrid grid;
  private final SearchState start;
  private final SearchState end;
  private final AHeuristic heuristic = AHeuristic.EUCLIEAN;
  private HashMap<Tile, SearchState> states;
  private Queue<SearchState> open;
  private boolean hasResult = false;

  /* METHODS */
  
  // constructors
  
  public PathSearch(Tile start_tile, Tile end_tile)
  {
    // initialise final attributes
    this.grid = start_tile.grid;
    this.start = new SearchState(start_tile, this);
    this.end = new SearchState(end_tile, this);
    
    // wrap graph vertices in exploration state objects
    states = new HashMap<Tile, SearchState>();
    for (Tile tile : grid)
    {
      // don't create state and end state twices
      SearchState state = (tile == start_tile) 
                          ? start : ((tile == end_tile) 
                                     ? end : new SearchState(tile, this));
      // save the states
      states.put(tile, state);
    }

    // add the start state to the open set
    open = new PriorityQueue<SearchState>();
    open.add(start);
  }
  
  // accessors
  
  int estimateCost(Tile tile)
  {
    return heuristic.estimate(tile.grid_position, end.tile.grid_position);
  }
  
  public Deque<Tile> getPath()
  {
    // if successful, generate path by reading back through tree
    return (hasResult) ? unfurl() : new LinkedList<Tile>();
  }
  
  /* IMPLEMNTS -- RUNNABLE */
  @Override
  public void run()
  {
    hasResult = search();
  }
  
  /* SUBROUTINES */

  private boolean search()
  {
    while (!open.isEmpty())
    {
      // expand from the open state that is currently cheapest
      SearchState x = open.poll();

      // have we reached the end?
      if (x.equals(end))
        return true;

      // try to expand each neighbour
      for (Tile t : grid.getNeighbours4(x.tile, Tile.EType.FLOOR))
        expand(x, t);

      // remember to close x now that all connections have been expanded
      x.closed = true;
    }
    return false;
  }

  private void expand(SearchState x, Tile t)
  {
    SearchState y = states.get(t);

    // closed states are no longer under consideration
    if (y.closed)
      return;

    // states not yet opened always link back to x
    if (!open.contains(y))
    {
      // set cost before adding to heap, or order will be wrong!
      y.setParent(x);
      open.add(y);
    }
    // states already open link back to x only if it's better
    else if (x.currentCost + 1 < y.currentCost)
    {
      // remove, reset cost and replace, or order will be wrong!
      open.remove(y);
      y.setParent(x);
      open.add(y);
    }
  }

  private Deque<Tile> unfurl()
  {
    Deque<Tile> result = new LinkedList<Tile>();

    // start at the end, trace backwards adding vertices
    SearchState current = end;
    while (current != null)
    {
      // add element to front, in order for list to be in the right order
      result.addFirst(current.tile);
      current = current.previous;
    }
    return result;
  }
}
