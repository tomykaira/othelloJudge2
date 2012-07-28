package views.users

/**
 * Created with IntelliJ IDEA.
 * User: tomykaira
 * Date: 7/28/12
 * Time: 6:25 PM
 * To change this template use File | Settings | File Templates.
 */

object MyHelpers {
  def urlEncode (string: String)(implicit codec: play.api.mvc.Codec) =
    java.net.URLEncoder.encode(string, codec.charset)
}
