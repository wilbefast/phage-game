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
import java.io.Serializable;
import wjd.amb.control.EUpdateResult;
import wjd.amb.model.AScene;
import wjd.amb.view.ICanvas;
import wjd.math.V2;
import wjd.phage.StrategyCamera;
import wjd.phage.play.PlayController;

/**
 * @author wdyce
 * @since 05-Oct-2012
 */
public class LevelScene extends AScene implements Serializable
{
  /* CONSTANTS */

  public static final V2 GRIDSIZE = new V2(100, 60);

  /* ATTRIBUTES */
  private Tile[][] tilegrid;
  private StrategyCamera camera;

  /* METHODS */
  // constructors
  public LevelScene()
  {
    // control
    setController(new PlayController(this));

    // model
    tilegrid = new Tile[(int) GRIDSIZE.y()][(int) GRIDSIZE.x()];
    for (int row = 0; row < tilegrid.length; row++)
      for (int col = 0; col < tilegrid[row].length; col++)
        tilegrid[row][col] = new Tile(row, col, (Math.random() > 0.5)
                                                ? Tile.EType.FLOOR : Tile.EType.WALL);

    /// FIXME -- create initial test unit
    tilegrid[0][0].setUnit(new Unit(tilegrid[0][0]));

    // view
    camera = new StrategyCamera(null); // FIXME add boundary
  }

  // mutators
  
  // accessors
  public Tile perspectiveToTile(V2 perspective_pos)
  {
    V2 grid_pos = camera.getGlobal(perspective_pos).shrink(Tile.SIZE).floor();
    return tilegrid[(int) grid_pos.y()][(int) grid_pos.x()];
  }
  /* IMPLEMENTS -- SCENE */
  
  @Override
  public EUpdateResult update(int t_delta)
  {
    // stay in this Scene if nothing interesting has happened
    return EUpdateResult.CONTINUE;
  }
  private static V2 min = new V2(), max = new V2();

  @Override
  public void render(ICanvas canvas)
  {
    if (!canvas.isCameraActive())
      canvas.setCamera(camera);

    // clear the screen
    canvas.clear();

    // find out what cells the camera can see
    camera.getVisibleGridCells(min, max, GRIDSIZE, Tile.ISIZE.x());

    // draw each cell
    for (int row = (int) Math.max(0, min.y());
         row < Math.min(tilegrid.length, max.y()); row++)
      for (int col = (int) Math.max(0, min.x());
           col < Math.min(tilegrid[row].length, max.x()); col++)
        tilegrid[row][col].render(canvas);
  }

  /* IMPLEMENTS -- SERIALIZABLE */

  private void readObject(ObjectInputStream in) 
    throws ClassNotFoundException, IOException
  {
    in.defaultReadObject();
  }

  private void writeObject(ObjectOutputStream out) 
    throws IOException
  {
    out.defaultWriteObject();
  }
}
