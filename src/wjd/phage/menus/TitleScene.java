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
package wjd.phage.menus;

import wjd.amb.control.Controller;
import wjd.amb.control.EUpdateResult;
import wjd.amb.control.IInput;
import wjd.amb.control.IInput.KeyPress;
import wjd.amb.model.AScene;
import wjd.amb.view.ICanvas;
import wjd.math.V2;
import wjd.phage.level.LevelScene;

/** 
* @author wdyce
* @since 10-Oct-2012
*/
public class TitleScene extends AScene
{
  /* CONSTANTS */
  private static final V2 HELLO_POS = new V2(100, 100);
  
  /* NESTING */
  private static class TitleController extends Controller
  {
    // attributes
    private TitleScene title;
    
    // methods
    TitleController(TitleScene title)
    {
      this.title = title;
    }
    // implementations
    @Override
    public EUpdateResult processKeyPress(KeyPress event)
    {    
      if(event.key == IInput.EKeyCode.ENTER && event.state)
      {
        title.setNext(new LevelScene());
        return EUpdateResult.STOP;
      }
      else if(event.key == IInput.EKeyCode.ESC && event.state)
        return EUpdateResult.STOP;
      return EUpdateResult.CONTINUE;
    }
  }
  
  /* METHODS */
  
  // constructors
  public TitleScene()
  {
    setController(new TitleController(this));
  }
  
  /* IMPLEMENTATIONS -- SCENE */

  @Override
  public EUpdateResult update(int t_delta)
  {
    return EUpdateResult.CONTINUE;
  }

  @Override
  public void render(ICanvas canvas)
  {
    canvas.clear();
    canvas.text("Hello Title", HELLO_POS);
  }
}
